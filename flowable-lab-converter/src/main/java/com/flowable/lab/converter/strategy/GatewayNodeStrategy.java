package com.flowable.lab.converter.strategy;

import com.flowable.lab.converter.ModelUtils;
import com.flowable.lab.converter.model.NodeJson;
import org.flowable.bpmn.model.*;
import org.flowable.bpmn.model.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GatewayNodeStrategy implements NodeStrategy {

    private static final Logger log = LoggerFactory.getLogger(GatewayNodeStrategy.class);

    @Override
    public FlowElement toElement(NodeJson node, Process process) {
        String subtype = node.getSubtype();
        return switch (subtype) {
            case "exclusive" -> {
                ExclusiveGateway g = new ExclusiveGateway();
                g.setId(node.getId());
                g.setName(node.getLabel());
                ModelUtils.applyExtensionElements(g, node.getExtensions());
                yield g;
            }
            case "parallel" -> {
                ParallelGateway g = new ParallelGateway();
                g.setId(node.getId());
                g.setName(node.getLabel());
                ModelUtils.applyExtensionElements(g, node.getExtensions());
                yield g;
            }
            case "inclusive" -> {
                InclusiveGateway g = new InclusiveGateway();
                g.setId(node.getId());
                g.setName(node.getLabel());
                ModelUtils.applyExtensionElements(g, node.getExtensions());
                yield g;
            }
            case "eventBased" -> {
                EventGateway g = new EventGateway();
                g.setId(node.getId());
                g.setName(node.getLabel());
                ModelUtils.applyExtensionElements(g, node.getExtensions());
                yield g;
            }
            default -> {
                log.warn("Unknown gateway subtype: {}, using ExclusiveGateway", subtype);
                ExclusiveGateway g = new ExclusiveGateway();
                g.setId(node.getId());
                g.setName(node.getLabel());
                yield g;
            }
        };
    }

    @Override
    public NodeJson toNode(FlowElement element, Map<String, GraphicInfo> locationMap) {
        NodeJson node = new NodeJson();
        node.setType("gateway");
        node.setId(element.getId());
        node.setLabel(element.getName());

        if (element instanceof ExclusiveGateway) node.setSubtype("exclusive");
        else if (element instanceof ParallelGateway) node.setSubtype("parallel");
        else if (element instanceof InclusiveGateway) node.setSubtype("inclusive");
        else if (element instanceof EventGateway) node.setSubtype("eventBased");

        if (element instanceof FlowNode fn) {
            node.setExtensions(ModelUtils.readExtensionElements(fn));
        }

        EventNodeStrategy.applyLocation(node, locationMap, element.getId());
        return node;
    }
}
