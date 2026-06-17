package com.flowable.lab.service.bpmn;

import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class BpmnInstanceService {

    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final TaskService taskService;

    public BpmnInstanceService(RuntimeService runtimeService, HistoryService historyService,
                               TaskService taskService) {
        this.runtimeService = runtimeService;
        this.historyService = historyService;
        this.taskService = taskService;
    }

    public ProcessInstance startProcessByKey(String processKey) {
        return runtimeService.startProcessInstanceByKey(processKey);
    }

    public ProcessInstance startProcessByKey(String processKey, Map<String, Object> variables) {
        return runtimeService.startProcessInstanceByKey(processKey, variables);
    }

    public ProcessInstance startProcessByKey(String processKey, String businessKey, Map<String, Object> variables) {
        return runtimeService.startProcessInstanceByKey(processKey, businessKey, variables);
    }

    public ProcessInstance getProcessInstance(String instanceId) {
        return runtimeService.createProcessInstanceQuery()
                .processInstanceId(instanceId)
                .includeProcessVariables()
                .singleResult();
    }

    public List<ProcessInstance> listRunningInstances() {
        return runtimeService.createProcessInstanceQuery()
                .orderByStartTime().desc()
                .includeProcessVariables()
                .list();
    }

    public List<ProcessInstance> listRunningInstancesByKey(String processKey) {
        return runtimeService.createProcessInstanceQuery()
                .processDefinitionKey(processKey)
                .orderByStartTime().desc()
                .includeProcessVariables()
                .list();
    }

    public void suspendInstance(String instanceId) {
        runtimeService.suspendProcessInstanceById(instanceId);
    }

    public void activateInstance(String instanceId) {
        runtimeService.activateProcessInstanceById(instanceId);
    }

    public void deleteInstance(String instanceId, String reason) {
        runtimeService.deleteProcessInstance(instanceId, reason);
    }

    public void setVariable(String instanceId, String name, Object value) {
        runtimeService.setVariable(instanceId, name, value);
    }

    public Object getVariable(String instanceId, String name) {
        return runtimeService.getVariable(instanceId, name);
    }

    public Map<String, Object> getVariables(String instanceId) {
        return runtimeService.getVariables(instanceId);
    }

    public List<Task> getActiveTasks(String instanceId) {
        return taskService.createTaskQuery()
                .processInstanceId(instanceId)
                .active()
                .list();
    }

    public HistoricProcessInstance getHistoricInstance(String instanceId) {
        return historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(instanceId)
                .includeProcessVariables()
                .singleResult();
    }

    public List<HistoricProcessInstance> listHistoricInstances() {
        return historyService.createHistoricProcessInstanceQuery()
                .orderByProcessInstanceStartTime().desc()
                .includeProcessVariables()
                .list();
    }

    public List<HistoricProcessInstance> listHistoricInstancesByKey(String processKey) {
        return historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey(processKey)
                .orderByProcessInstanceStartTime().desc()
                .includeProcessVariables()
                .list();
    }
}
