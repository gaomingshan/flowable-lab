package com.flowable.lab.converter;

import com.flowable.lab.converter.model.EdgeJson;
import com.flowable.lab.converter.model.NodeDimensions;
import com.flowable.lab.converter.model.NodeJson;
import com.flowable.lab.converter.model.ProcessJson;
import com.flowable.lab.converter.model.ProcessJson.ProcessInfo;
import com.flowable.lab.converter.strategy.*;
import jakarta.annotation.PostConstruct;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.*;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.common.engine.api.io.InputStreamProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class BpmnEditorJsonConverter {

    private static final Logger log = LoggerFactory.getLogger(BpmnEditorJsonConverter.class);

    private final Map<String, NodeStrategy> strategies = new LinkedHashMap<>();
    private final EventNodeStrategy eventStrategy;
    private final TaskNodeStrategy taskStrategy;
    private final GatewayNodeStrategy gatewayStrategy;
    private final SubProcessNodeStrategy subProcessStrategy;

    public BpmnEditorJsonConverter(EventNodeStrategy eventStrategy, TaskNodeStrategy taskStrategy,
                                   GatewayNodeStrategy gatewayStrategy, SubProcessNodeStrategy subProcessStrategy) {
        this.eventStrategy = eventStrategy;
        this.taskStrategy = taskStrategy;
        this.gatewayStrategy = gatewayStrategy;
        this.subProcessStrategy = subProcessStrategy;
    }

    @PostConstruct
    void init() {
        strategies.put("event", eventStrategy);
        strategies.put("task", taskStrategy);
        strategies.put("gateway", gatewayStrategy);
        strategies.put("subProcess", subProcessStrategy);

        subProcessStrategy.setChildElementConverter(this::toFlowElements);
    }

    // ========== Top-level API ==========

    public String toBpmnXml(ProcessJson json) {
        BpmnModel bpmnModel = toBpmnModel(json);
        return new String(new BpmnXMLConverter().convertToXML(bpmnModel), StandardCharsets.UTF_8);
    }

    public ProcessJson fromBpmnXml(String xml) {
        byte[] bytes = xml.getBytes(StandardCharsets.UTF_8);
        BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(
                (InputStreamProvider) () -> new ByteArrayInputStream(bytes),
                false, false, "UTF-8");
        return toProcessJson(bpmnModel);
    }

    // ========== JSON → BpmnModel ==========

    public BpmnModel toBpmnModel(ProcessJson json) {
        BpmnModel model = new BpmnModel();
        Process process = new Process();
        ProcessInfo info = json.getProcess();
        process.setId(info.getId());
        process.setName(info.getName());
        process.setExecutable(info.isExecutable());

        if (info.getDocumentation() != null && !info.getDocumentation().isEmpty()) {
            process.setDocumentation(info.getDocumentation());
        }
        ModelUtils.applyExtensionElements(process, info.getExtensions());

        model.addProcess(process);

        for (NodeJson node : json.getNodes()) {
            List<FlowElement> elements = toFlowElements(node);
            for (FlowElement el : elements) {
                process.addFlowElement(el);
            }
        }

        for (EdgeJson edge : json.getEdges()) {
            process.addFlowElement(toSequenceFlow(edge));
        }

        for (NodeJson node : json.getNodes()) {
            addGraphicInfo(model, node);
        }
        for (EdgeJson edge : json.getEdges()) {
            addFlowGraphicInfo(model, edge, json.getNodes());
        }

        return model;
    }

    private List<FlowElement> toFlowElements(NodeJson node) {
        NodeStrategy strategy = strategies.get(node.getType());
        if (strategy == null) {
            log.warn("Unknown node type: {}, treating as task", node.getType());
            strategy = taskStrategy;
        }

        FlowElement element = strategy.toElement(node, null);
        return List.of(element);
    }

    // ========== BpmnModel → JSON ==========

    public ProcessJson toProcessJson(BpmnModel model) {
        ProcessJson json = new ProcessJson();
        List<Process> processes = model.getProcesses();
        if (processes == null || processes.isEmpty()) return json;
        Process process = processes.get(0);

        ProcessInfo info = json.getProcess();
        info.setId(process.getId())
                .setName(process.getName())
                .setExecutable(process.isExecutable());
        if (process.getDocumentation() != null) {
            info.setDocumentation(process.getDocumentation());
        }
        info.setExtensions(ModelUtils.readExtensionElements(process));

        Map<String, GraphicInfo> locationMap = model.getLocationMap();

        for (FlowElement element : process.getFlowElements()) {
            if (element instanceof SequenceFlow sf) {
                json.getEdges().add(toEdgeJson(sf));
            } else {
                NodeStrategy strategy = resolveStrategy(element);
                if (strategy != null) {
                    json.getNodes().add(strategy.toNode(element, locationMap));
                }
            }
        }

        return json;
    }

    private NodeStrategy resolveStrategy(FlowElement element) {
        if (element instanceof StartEvent) return eventStrategy;
        if (element instanceof EndEvent) return eventStrategy;
        if (element instanceof BoundaryEvent) return eventStrategy;
        if (element instanceof IntermediateCatchEvent) return eventStrategy;
        if (element instanceof UserTask) return taskStrategy;
        if (element instanceof ServiceTask) return taskStrategy;
        if (element instanceof ScriptTask) return taskStrategy;
        if (element instanceof ReceiveTask) return taskStrategy;
        if (element instanceof CallActivity) return taskStrategy;
        if (element instanceof ExclusiveGateway) return gatewayStrategy;
        if (element instanceof ParallelGateway) return gatewayStrategy;
        if (element instanceof InclusiveGateway) return gatewayStrategy;
        if (element instanceof EventGateway) return gatewayStrategy;
        if (element instanceof SubProcess) return subProcessStrategy;
        if (element instanceof Transaction) return subProcessStrategy;
        log.warn("Unrecognized FlowElement type: {}, falling back to task", element.getClass().getSimpleName());
        return taskStrategy;
    }

    // ========== SequenceFlow ==========

    private SequenceFlow toSequenceFlow(EdgeJson edge) {
        SequenceFlow flow = new SequenceFlow(edge.getSource(), edge.getTarget());
        flow.setId(edge.getId());
        flow.setName(edge.getLabel());
        Map<String, Object> props = edge.getProperties();
        if (props != null) {
            String condition = ModelUtils.getString(props, "conditionExpression");
            if (condition != null && !condition.isEmpty()) {
                flow.setConditionExpression(condition);
            }
        }
        Map<String, Object> exts = edge.getExtensions();
        if (exts != null && !exts.isEmpty()) {
            ModelUtils.applyExtensionElements(flow, exts);
        }
        return flow;
    }

    private EdgeJson toEdgeJson(SequenceFlow flow) {
        EdgeJson edge = new EdgeJson();
        edge.setId(flow.getId());
        edge.setSource(flow.getSourceRef());
        edge.setTarget(flow.getTargetRef());
        edge.setLabel(flow.getName());
        if (flow.getConditionExpression() != null) {
            edge.getProperties().put("conditionExpression", flow.getConditionExpression());
        }
        edge.setExtensions(ModelUtils.readExtensionElements(flow));
        return edge;
    }

    // ========== GraphicInfo (BPMNDI) ==========

    private void addGraphicInfo(BpmnModel model, NodeJson node) {
        if ("subProcess".equals(node.getType())) return;
        GraphicInfo gi = new GraphicInfo();
        NodeDimensions dims = NodeDimensions.forNode(node);
        double w = node.getWidth() != null ? node.getWidth() : dims.getDefaultWidth();
        double h = node.getHeight() != null ? node.getHeight() : dims.getDefaultHeight();
        gi.setX(node.getX() - w / 2);
        gi.setY(node.getY() - h / 2);
        gi.setWidth(w);
        gi.setHeight(h);
        model.addGraphicInfo(node.getId(), gi);
    }

    private void addFlowGraphicInfo(BpmnModel model, EdgeJson edge, List<NodeJson> allNodes) {
        GraphicInfo sourceGi = model.getLocationMap().get(edge.getSource());
        GraphicInfo targetGi = model.getLocationMap().get(edge.getTarget());
        if (sourceGi == null || targetGi == null) return;

        List<GraphicInfo> points = new ArrayList<>();
        GraphicInfo start = new GraphicInfo();
        start.setX(sourceGi.getX() + sourceGi.getWidth() / 2);
        start.setY(sourceGi.getY() + sourceGi.getHeight() / 2);
        points.add(start);

        GraphicInfo end = new GraphicInfo();
        end.setX(targetGi.getX() + targetGi.getWidth() / 2);
        end.setY(targetGi.getY() + targetGi.getHeight() / 2);
        points.add(end);

        model.getFlowLocationMap().put(edge.getId(), points);
    }
}
