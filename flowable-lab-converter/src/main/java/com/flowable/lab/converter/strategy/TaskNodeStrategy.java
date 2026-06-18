package com.flowable.lab.converter.strategy;

import com.flowable.lab.converter.ModelUtils;
import com.flowable.lab.converter.model.NodeJson;
import org.flowable.bpmn.model.*;
import org.flowable.bpmn.model.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

@Component
public class TaskNodeStrategy implements NodeStrategy {

    private static final Logger log = LoggerFactory.getLogger(TaskNodeStrategy.class);

    @Override
    public FlowElement toElement(NodeJson node, Process process) {
        String subtype = node.getSubtype();
        return switch (subtype) {
            case "user" -> {
                UserTask t = new UserTask();
                t.setId(node.getId());
                t.setName(node.getLabel());
                applyUserTaskProperties(t, node.getProperties());
                ModelUtils.applyExtensionElements(t, node.getExtensions());
                yield t;
            }
            case "service" -> {
                ServiceTask t = new ServiceTask();
                t.setId(node.getId());
                t.setName(node.getLabel());
                String impl = ModelUtils.getString(node.getProperties(), "implementation");
                if (impl != null) t.setImplementation("${" + impl + "}");
                ModelUtils.applyExtensionElements(t, node.getExtensions());
                yield t;
            }
            case "script" -> {
                ScriptTask t = new ScriptTask();
                t.setId(node.getId());
                t.setName(node.getLabel());
                t.setScript(ModelUtils.getString(node.getProperties(), "script"));
                ModelUtils.applyExtensionElements(t, node.getExtensions());
                yield t;
            }
            case "receive" -> {
                ReceiveTask t = new ReceiveTask();
                t.setId(node.getId());
                t.setName(node.getLabel());
                ModelUtils.applyExtensionElements(t, node.getExtensions());
                yield t;
            }
            case "callActivity" -> {
                CallActivity t = new CallActivity();
                t.setId(node.getId());
                t.setName(node.getLabel());
                t.setCalledElement(ModelUtils.getString(node.getProperties(), "calledElement"));
                ModelUtils.applyExtensionElements(t, node.getExtensions());
                yield t;
            }
            default -> {
                log.warn("Unknown task subtype: {}, using UserTask", subtype);
                UserTask t = new UserTask();
                t.setId(node.getId());
                t.setName(node.getLabel());
                yield t;
            }
        };
    }

    @Override
    public NodeJson toNode(FlowElement element, Map<String, GraphicInfo> locationMap) {
        NodeJson node = new NodeJson();
        node.setType("task");
        node.setId(element.getId());
        node.setLabel(element.getName());

        if (element instanceof UserTask) node.setSubtype("user");
        else if (element instanceof ServiceTask) node.setSubtype("service");
        else if (element instanceof ScriptTask) node.setSubtype("script");
        else if (element instanceof ReceiveTask) node.setSubtype("receive");
        else if (element instanceof CallActivity) node.setSubtype("callActivity");

        if (element instanceof FlowNode fn) {
            node.setExtensions(ModelUtils.readExtensionElements(fn));
        }
        toProperties(element, node);

        EventNodeStrategy.applyLocation(node, locationMap, element.getId());
        return node;
    }

    void toProperties(FlowElement element, NodeJson node) {
        if (!(element instanceof UserTask ut)) return;
        Map<String, Object> props = node.getProperties();
        put(props, "assignee", ut.getAssignee());
        put(props, "owner", ut.getOwner());
        put(props, "priority", ut.getPriority());
        put(props, "formKey", ut.getFormKey());
        put(props, "category", ut.getCategory());
        put(props, "dueDate", ut.getDueDate());
        if (ut.getCandidateUsers() != null && !ut.getCandidateUsers().isEmpty())
            props.put("candidateUsers", String.join(",", ut.getCandidateUsers()));
        if (ut.getCandidateGroups() != null && !ut.getCandidateGroups().isEmpty())
            props.put("candidateGroups", String.join(",", ut.getCandidateGroups()));
    }

    private void applyUserTaskProperties(UserTask task, Map<String, Object> props) {
        if (props == null) return;
        task.setAssignee(ModelUtils.getString(props, "assignee"));
        task.setOwner(ModelUtils.getString(props, "owner"));
        Integer p = ModelUtils.getInteger(props, "priority");
        task.setPriority(p != null ? p.toString() : null);
        task.setCategory(ModelUtils.getString(props, "category"));
        task.setFormKey(ModelUtils.getString(props, "formKey"));
        task.setDueDate(ModelUtils.getString(props, "dueDate"));
        String candidateUsers = ModelUtils.getString(props, "candidateUsers");
        if (candidateUsers != null) task.setCandidateUsers(Arrays.asList(candidateUsers.split(",")));
        String candidateGroups = ModelUtils.getString(props, "candidateGroups");
        if (candidateGroups != null) task.setCandidateGroups(Arrays.asList(candidateGroups.split(",")));
    }

    private void put(Map<String, Object> map, String key, Object value) {
        if (value != null) map.put(key, value);
    }
}
