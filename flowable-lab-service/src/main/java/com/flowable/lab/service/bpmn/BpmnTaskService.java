package com.flowable.lab.service.bpmn;

import org.flowable.engine.HistoryService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class BpmnTaskService {

    private final TaskService taskService;
    private final HistoryService historyService;

    public BpmnTaskService(TaskService taskService, HistoryService historyService) {
        this.taskService = taskService;
        this.historyService = historyService;
    }

    public List<Task> listTodoTasks(String assignee) {
        return taskService.createTaskQuery()
                .taskAssignee(assignee)
                .active()
                .orderByTaskCreateTime().desc()
                .list();
    }

    public List<Task> listTodoTasksByGroup(String groupId) {
        return taskService.createTaskQuery()
                .taskCandidateGroup(groupId)
                .active()
                .orderByTaskCreateTime().desc()
                .list();
    }

    public List<Task> listUnassignedTasks() {
        return taskService.createTaskQuery()
                .taskUnassigned()
                .active()
                .orderByTaskCreateTime().desc()
                .list();
    }

    public List<Task> listGroupTasks(String groupId) {
        return taskService.createTaskQuery()
                .taskCandidateGroup(groupId)
                .active()
                .orderByTaskCreateTime().desc()
                .list();
    }

    public Task getTask(String taskId) {
        return taskService.createTaskQuery()
                .taskId(taskId)
                .includeProcessVariables()
                .includeTaskLocalVariables()
                .singleResult();
    }

    public void completeTask(String taskId) {
        taskService.complete(taskId);
    }

    public void completeTask(String taskId, Map<String, Object> variables) {
        taskService.complete(taskId, variables);
    }

    public void claimTask(String taskId, String userId) {
        taskService.claim(taskId, userId);
    }

    public void unclaimTask(String taskId) {
        taskService.unclaim(taskId);
    }

    public void delegateTask(String taskId, String delegateUserId) {
        taskService.delegateTask(taskId, delegateUserId);
    }

    public void resolveTask(String taskId) {
        taskService.resolveTask(taskId);
    }

    public void transferTask(String taskId, String newAssignee) {
        taskService.setAssignee(taskId, newAssignee);
    }

    public void setTaskVariable(String taskId, String name, Object value) {
        taskService.setVariable(taskId, name, value);
    }

    public Map<String, Object> getTaskVariables(String taskId) {
        return taskService.getVariables(taskId);
    }

    public void addComment(String taskId, String processInstanceId, String message) {
        taskService.addComment(taskId, processInstanceId, message);
    }

    public List<HistoricTaskInstance> listDoneTasks(String assignee) {
        return historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(assignee)
                .finished()
                .orderByHistoricTaskInstanceEndTime().desc()
                .list();
    }
}
