package com.flowable.lab.service.bpmn;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.SubProcess;
import org.flowable.bpmn.model.GraphicInfo;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.image.impl.DefaultProcessDiagramGenerator;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
public class BpmnDiagramService {

    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;

    public BpmnDiagramService(RepositoryService repositoryService, RuntimeService runtimeService) {
        this.repositoryService = repositoryService;
        this.runtimeService = runtimeService;
    }

    public InputStream generateDiagram(String processDefinitionId) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        if (bpmnModel == null) {
            return null;
        }
        ensureGraphicInfo(bpmnModel);
        ProcessDiagramGenerator generator = new DefaultProcessDiagramGenerator();
        return generator.generateDiagram(bpmnModel, "png", Collections.emptyList(),
                Collections.emptyList(), false);
    }

    public InputStream generateHighlightedDiagram(String processDefinitionId, String processInstanceId) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        if (bpmnModel == null) {
            return null;
        }
        ensureGraphicInfo(bpmnModel);
        List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstanceId);
        ProcessDiagramGenerator generator = new DefaultProcessDiagramGenerator();
        return generator.generateDiagram(bpmnModel, "png", activeActivityIds,
                Collections.emptyList(), false);
    }

    public InputStream generateHighlightedDiagramWithHistory(String processDefinitionId, String processInstanceId) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        if (bpmnModel == null) {
            return null;
        }
        ensureGraphicInfo(bpmnModel);
        List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstanceId);
        List<String> completedActivityIds = Collections.emptyList();
        ProcessDiagramGenerator generator = new DefaultProcessDiagramGenerator();
        return generator.generateDiagram(bpmnModel, "png", activeActivityIds,
                completedActivityIds, false);
    }

    public static void ensureGraphicInfo(BpmnModel model) {
        model.getLocationMap().clear();
        model.getFlowLocationMap().clear();
        int[] cursor = {50, 50};
        for (Process process : model.getProcesses()) {
            layoutFlowElements(model, process.getFlowElements(), cursor);
            cursor[0] = 50;
            cursor[1] += 120;
        }
    }

    private static void layoutFlowElements(BpmnModel model, Collection<FlowElement> flowElements, int[] cursor) {
        for (FlowElement element : flowElements) {
            if (element instanceof SequenceFlow) {
                continue;
            }
            if (element instanceof SubProcess sub) {
                GraphicInfo sgi = new GraphicInfo();
                sgi.setX(cursor[0]);
                sgi.setY(cursor[1]);
                sgi.setWidth(100);
                sgi.setHeight(80);
                model.addGraphicInfo(element.getId(), sgi);
                cursor[0] += 150;
                layoutFlowElements(model, sub.getFlowElements(), cursor);
                continue;
            }
            GraphicInfo gi = new GraphicInfo();
            gi.setX(cursor[0]);
            gi.setY(cursor[1]);
            gi.setWidth(100);
            gi.setHeight(80);
            model.addGraphicInfo(element.getId(), gi);
            cursor[0] += 150;
        }
        for (FlowElement element : flowElements) {
            if (!(element instanceof SequenceFlow sf)) {
                continue;
            }
            GraphicInfo sourceGi = model.getLocationMap().get(sf.getSourceRef());
            GraphicInfo targetGi = model.getLocationMap().get(sf.getTargetRef());
            if (sourceGi == null || targetGi == null) {
                continue;
            }
            List<GraphicInfo> points = new java.util.ArrayList<>();
            GraphicInfo start = new GraphicInfo();
            start.setX(sourceGi.getX() + sourceGi.getWidth() / 2.0);
            start.setY(sourceGi.getY() + sourceGi.getHeight() / 2.0);
            points.add(start);
            GraphicInfo end = new GraphicInfo();
            end.setX(targetGi.getX() + targetGi.getWidth() / 2.0);
            end.setY(targetGi.getY() + targetGi.getHeight() / 2.0);
            points.add(end);
            model.getFlowLocationMap().put(sf.getId(), points);
        }
    }
}
