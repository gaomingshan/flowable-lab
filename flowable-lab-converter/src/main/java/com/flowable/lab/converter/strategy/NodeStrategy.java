package com.flowable.lab.converter.strategy;

import com.flowable.lab.converter.model.NodeJson;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.GraphicInfo;
import org.flowable.bpmn.model.Process;

import java.util.Map;

public interface NodeStrategy {

    FlowElement toElement(NodeJson node, Process process);

    NodeJson toNode(FlowElement element, Map<String, GraphicInfo> locationMap);
}
