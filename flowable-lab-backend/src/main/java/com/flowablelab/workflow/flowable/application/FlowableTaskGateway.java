package com.flowablelab.workflow.flowable.application;

import lombok.RequiredArgsConstructor;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FlowableTaskGateway {

    private final TaskService taskService;
    private final HistoryService historyService;
    private final RuntimeService runtimeService;

    public List<Task> listUnclaimedTasks(String userId) {
        return taskService.createTaskQuery()
                .taskCandidateUser(userId)
                .taskUnassigned()
                .orderByTaskCreateTime()
                .desc()
                .list();
    }

    public List<Task> listTodoTasks(String userId) {
        return taskService.createTaskQuery()
                .taskAssignee(userId)
                .orderByTaskCreateTime()
                .desc()
                .list();
    }

    public List<HistoricTaskInstance> listDoneTasks(String userId) {
        return historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(userId)
                .finished()
                .orderByHistoricTaskInstanceEndTime()
                .desc()
                .list();
    }

    public List<HistoricProcessInstance> listInitiatedInstances(String starterId) {
        return historyService.createHistoricProcessInstanceQuery()
                .includeProcessVariables()
                .variableValueEquals("starterId", starterId)
                .orderByProcessInstanceStartTime()
                .desc()
                .list();
    }

    public Task getRuntimeTask(String taskId) {
        return taskService.createTaskQuery().taskId(taskId).singleResult();
    }

    public HistoricTaskInstance getHistoricTask(String taskId) {
        return historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
    }

    public HistoricProcessInstance getHistoricProcessInstance(String processInstanceId) {
        return historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .includeProcessVariables()
                .singleResult();
    }

    public Map<String, Object> getProcessVariables(String processInstanceId) {
        return runtimeService.getVariables(processInstanceId);
    }

    public void claimTask(String taskId, String userId) {
        taskService.claim(taskId, userId);
    }

    public void completeTask(String taskId, Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) {
            taskService.complete(taskId);
            return;
        }
        taskService.complete(taskId, variables);
    }

    public List<IdentityLink> getIdentityLinksForTask(String taskId) {
        return taskService.getIdentityLinksForTask(taskId);
    }
}
