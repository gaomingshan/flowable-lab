package com.flowable.lab.converter.strategy;

import com.flowable.lab.converter.ModelUtils;
import com.flowable.lab.converter.model.NodeDimensions;
import com.flowable.lab.converter.model.NodeJson;
import org.flowable.bpmn.model.*;
import org.flowable.bpmn.model.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EventNodeStrategy implements NodeStrategy {

    private static final Logger log = LoggerFactory.getLogger(EventNodeStrategy.class);

    @Override
    public FlowElement toElement(NodeJson node, Process process) {
        String subtype = node.getSubtype();
        return switch (subtype) {
            case "start" -> {
                StartEvent e = new StartEvent();
                e.setId(node.getId());
                e.setName(node.getLabel());
                ModelUtils.applyExtensionElements(e, node.getExtensions());
                yield e;
            }
            case "end" -> {
                EndEvent e = new EndEvent();
                e.setId(node.getId());
                e.setName(node.getLabel());
                ModelUtils.applyExtensionElements(e, node.getExtensions());
                yield e;
            }
            case "boundary" -> {
                BoundaryEvent e = new BoundaryEvent();
                e.setId(node.getId());
                e.setName(node.getLabel());
                String attachedTo = ModelUtils.getString(node.getProperties(), "attachedToRef");
                if (attachedTo != null) e.setAttachedToRefId(attachedTo);
                e.setCancelActivity(ModelUtils.getBoolean(node.getProperties(), "cancelActivity", true));
                ModelUtils.applyExtensionElements(e, node.getExtensions());
                yield e;
            }
            case "intermediateCatch" -> {
                IntermediateCatchEvent e = new IntermediateCatchEvent();
                e.setId(node.getId());
                e.setName(node.getLabel());
                ModelUtils.applyExtensionElements(e, node.getExtensions());
                yield e;
            }
            default -> {
                log.warn("Unknown event subtype: {}, using StartEvent", subtype);
                StartEvent e = new StartEvent();
                e.setId(node.getId());
                e.setName(node.getLabel());
                yield e;
            }
        };
    }

    @Override
    public NodeJson toNode(FlowElement element, Map<String, GraphicInfo> locationMap) {
        NodeJson node = new NodeJson();
        node.setType("event");
        node.setId(element.getId());
        node.setLabel(element.getName());

        if (element instanceof StartEvent) node.setSubtype("start");
        else if (element instanceof EndEvent) node.setSubtype("end");
        else if (element instanceof BoundaryEvent) node.setSubtype("boundary");
        else if (element instanceof IntermediateCatchEvent) node.setSubtype("intermediateCatch");

        if (element instanceof FlowNode fn) {
            node.setExtensions(ModelUtils.readExtensionElements(fn));
            toProperties(element, node);
        }

        applyLocation(node, locationMap, element.getId());
        return node;
    }

    void toProperties(FlowElement element, NodeJson node) {
        if (element instanceof BoundaryEvent be) {
            Map<String, Object> props = node.getProperties();
            if (be.getAttachedToRefId() != null) props.put("attachedToRef", be.getAttachedToRefId());
            props.put("cancelActivity", be.isCancelActivity());
        }
    }

    static void applyLocation(NodeJson node, Map<String, GraphicInfo> locationMap, String elementId) {
        if (locationMap == null) return;
        GraphicInfo gi = locationMap.get(elementId);
        if (gi == null) return;
        String type = node.getType();
        NodeDimensions dims = NodeDimensions.valueOf(type.toUpperCase());
        double w = gi.getWidth() > 0 ? gi.getWidth() : dims.getDefaultWidth();
        double h = gi.getHeight() > 0 ? gi.getHeight() : dims.getDefaultHeight();
        node.setX(gi.getX() + w / 2);
        node.setY(gi.getY() + h / 2);
        node.setWidth(w);
        node.setHeight(h);
    }
}
