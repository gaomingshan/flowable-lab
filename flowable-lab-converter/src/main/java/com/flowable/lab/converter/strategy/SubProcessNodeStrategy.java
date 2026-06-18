package com.flowable.lab.converter.strategy;

import com.flowable.lab.converter.model.EdgeJson;
import com.flowable.lab.converter.model.NodeJson;
import com.flowable.lab.converter.model.ProcessJson;
import org.flowable.bpmn.model.*;
import org.flowable.bpmn.model.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class SubProcessNodeStrategy implements NodeStrategy {

    private static final Logger log = LoggerFactory.getLogger(SubProcessNodeStrategy.class);

    private Function<NodeJson, List<FlowElement>> childElementConverter;

    public void setChildElementConverter(Function<NodeJson, List<FlowElement>> converter) {
        this.childElementConverter = converter;
    }

    @Override
    public FlowElement toElement(NodeJson node, Process process) {
        String subtype = node.getSubtype();

        if ("event".equals(subtype)) {
            EventSubProcess sub = new EventSubProcess();
            sub.setId(node.getId());
            sub.setName(node.getLabel());
            addChildren(sub, node);
            return sub;
        }

        if ("transaction".equals(subtype)) {
            Transaction t = new Transaction();
            t.setId(node.getId());
            t.setName(node.getLabel());
            addChildren(t, node);
            return t;
        }

        SubProcess sub = new SubProcess();
        sub.setId(node.getId());
        sub.setName(node.getLabel());
        addChildren(sub, node);
        return sub;
    }

    private void addChildren(SubProcess sub, NodeJson node) {
        if (node.getProcess() == null) return;
        if (childElementConverter == null) return;
        for (NodeJson child : node.getProcess().getNodes()) {
            List<FlowElement> elements = childElementConverter.apply(child);
            for (FlowElement el : elements) {
                sub.addFlowElement(el);
            }
        }
        for (EdgeJson edge : node.getProcess().getEdges()) {
            SequenceFlow flow = new SequenceFlow(edge.getSource(), edge.getTarget());
            flow.setId(edge.getId());
            flow.setName(edge.getLabel());
            sub.addFlowElement(flow);
        }
    }

    @Override
    public NodeJson toNode(FlowElement element, Map<String, GraphicInfo> locationMap) {
        NodeJson node = new NodeJson();
        node.setId(element.getId());
        node.setLabel(element.getName());

        if (element instanceof EventSubProcess) {
            node.setType("subProcess");
            node.setSubtype("event");
        } else if (element instanceof Transaction) {
            node.setType("subProcess");
            node.setSubtype("transaction");
        } else if (element instanceof SubProcess) {
            node.setType("subProcess");
            node.setSubtype("embedded");
        }

        if (element instanceof FlowNode fn) {
        }

        ProcessJson subProcess = new ProcessJson();
        ProcessJson.ProcessInfo info = subProcess.getProcess();
        info.setId(element.getId() + "_sub");
        info.setName(node.getLabel() != null ? node.getLabel() + "子图" : "子图");

        if (element instanceof SubProcess sp) {
            for (FlowElement child : sp.getFlowElements()) {
                if (child instanceof SequenceFlow sf) {
                    EdgeJson edge = toEdgeJson(sf);
                    subProcess.getEdges().add(edge);
                } else {
                    subProcess.getNodes().add(toChildNode(child, locationMap));
                }
            }
        }

        node.setProcess(subProcess);

        EventNodeStrategy.applyLocation(node, locationMap, element.getId());
        return node;
    }

    private NodeJson toChildNode(FlowElement element, Map<String, GraphicInfo> locationMap) {
        NodeJson child = new NodeJson();
        child.setId(element.getId());
        child.setLabel(element.getName());

        if (element instanceof StartEvent) { child.setType("event"); child.setSubtype("start"); }
        else if (element instanceof EndEvent) { child.setType("event"); child.setSubtype("end"); }
        else if (element instanceof UserTask) { child.setType("task"); child.setSubtype("user"); }
        else if (element instanceof ServiceTask) { child.setType("task"); child.setSubtype("service"); }
        else if (element instanceof ExclusiveGateway) { child.setType("gateway"); child.setSubtype("exclusive"); }
        else if (element instanceof ParallelGateway) { child.setType("gateway"); child.setSubtype("parallel"); }
        else if (element instanceof InclusiveGateway) { child.setType("gateway"); child.setSubtype("inclusive"); }
        else { child.setType("task"); child.setSubtype("user"); }

        if (locationMap != null) {
            EventNodeStrategy.applyLocation(child, locationMap, element.getId());
        }
        return child;
    }

    private EdgeJson toEdgeJson(SequenceFlow flow) {
        EdgeJson edge = new EdgeJson();
        edge.setId(flow.getId());
        edge.setSource(flow.getSourceRef());
        edge.setTarget(flow.getTargetRef());
        edge.setLabel(flow.getName());
        return edge;
    }
}
