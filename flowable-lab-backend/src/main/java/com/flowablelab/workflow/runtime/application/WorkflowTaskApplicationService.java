package com.flowablelab.workflow.runtime.application;

import com.flowablelab.shared.api.CurrentUser;
import com.flowablelab.workflow.flowable.application.FlowableTaskGateway;
import com.flowablelab.workflow.query.api.dto.WorkflowInstanceSummaryResponse;
import com.flowablelab.workflow.query.api.dto.WorkflowTaskDetailResponse;
import com.flowablelab.workflow.query.api.dto.WorkflowTaskSummaryResponse;
import com.flowablelab.workflow.runtime.api.dto.WorkflowTaskActionResponse;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WorkflowTaskApplicationService {

    private final FlowableTaskGateway flowableTaskGateway;
    private final ApplicationEventPublisher applicationEventPublisher;

    public List<WorkflowTaskSummaryResponse> listUnclaimedTasks(String userId) {
        String operator = resolveUser(userId);
        return flowableTaskGateway.listUnclaimedTasks(operator).stream()
                .map(task -> toTaskSummary(task, "UNCLAIMED"))
                .toList();
    }

    public List<WorkflowTaskSummaryResponse> listTodoTasks(String userId) {
        String operator = resolveUser(userId);
        return flowableTaskGateway.listTodoTasks(operator).stream()
                .map(task -> toTaskSummary(task, "CLAIMED"))
                .toList();
    }

    public List<WorkflowTaskSummaryResponse> listDoneTasks(String userId) {
        String operator = resolveUser(userId);
        return flowableTaskGateway.listDoneTasks(operator).stream()
                .map(this::toHistoricTaskSummary)
                .toList();
    }

    public List<WorkflowInstanceSummaryResponse> listInitiatedInstances(String userId) {
        String operator = resolveUser(userId);
        return flowableTaskGateway.listInitiatedInstances(operator).stream()
                .map(this::toInstanceSummary)
                .toList();
    }

    public WorkflowTaskActionResponse claimTask(String taskId, String userId) {
        String operator = resolveUser(userId);
        Task task = requireTask(taskId);
        flowableTaskGateway.claimTask(taskId, operator);
        Task claimedTask = requireTask(taskId);

        applicationEventPublisher.publishEvent(new WorkflowTaskClaimedEvent(
                claimedTask.getId(),
                claimedTask.getProcessInstanceId(),
                claimedTask.getTaskDefinitionKey(),
                operator
        ));

        return WorkflowTaskActionResponse.builder()
                .taskId(claimedTask.getId())
                .action("claim")
                .assignee(claimedTask.getAssignee())
                .processInstanceId(claimedTask.getProcessInstanceId())
                .status("CLAIMED")
                .build();
    }

    public WorkflowTaskActionResponse completeTask(String taskId, String userId, Map<String, Object> variables) {
        String operator = resolveUser(userId);
        Task task = requireTask(taskId);
        if (task.getAssignee() == null || task.getAssignee().isBlank()) {
            throw new IllegalArgumentException("任务尚未认领，不能直接办理: " + taskId);
        }
        if (!operator.equals(task.getAssignee())) {
            throw new IllegalArgumentException("当前用户不是任务办理人: " + taskId);
        }

        flowableTaskGateway.completeTask(taskId, variables);
        applicationEventPublisher.publishEvent(new WorkflowTaskCompletedEvent(
                task.getId(),
                task.getProcessInstanceId(),
                task.getTaskDefinitionKey(),
                operator
        ));

        return WorkflowTaskActionResponse.builder()
                .taskId(task.getId())
                .action("complete")
                .assignee(operator)
                .processInstanceId(task.getProcessInstanceId())
                .status("COMPLETED")
                .build();
    }

    public WorkflowInstanceSummaryResponse getInstanceSummary(String processInstanceId) {
        HistoricProcessInstance instance = flowableTaskGateway.getHistoricProcessInstance(processInstanceId);
        if (instance == null) {
            throw new IllegalArgumentException("流程实例不存在: " + processInstanceId);
        }
        return toInstanceSummary(instance);
    }

    public WorkflowTaskDetailResponse getTaskDetail(String taskId) {
        Task runtimeTask = flowableTaskGateway.getRuntimeTask(taskId);
        if (runtimeTask != null) {
            WorkflowTaskSummaryResponse taskSummary = toTaskSummary(runtimeTask, runtimeTask.getAssignee() == null ? "UNCLAIMED" : "CLAIMED");
            WorkflowInstanceSummaryResponse instanceSummary = getInstanceSummary(runtimeTask.getProcessInstanceId());
            return WorkflowTaskDetailResponse.builder()
                    .task(taskSummary)
                    .instance(instanceSummary)
                    .variables(safeVariables(runtimeTask.getProcessInstanceId()))
                    .candidateUsers(taskSummary.getCandidateUsers())
                    .candidateGroups(taskSummary.getCandidateGroups())
                    .build();
        }

        HistoricTaskInstance historicTask = flowableTaskGateway.getHistoricTask(taskId);
        if (historicTask == null) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }
        WorkflowTaskSummaryResponse taskSummary = toHistoricTaskSummary(historicTask);
        WorkflowInstanceSummaryResponse instanceSummary = getInstanceSummary(historicTask.getProcessInstanceId());
        return WorkflowTaskDetailResponse.builder()
                .task(taskSummary)
                .instance(instanceSummary)
                .variables(instanceSummary == null ? Map.of() : safeHistoricVariables(historicTask.getProcessInstanceId()))
                .candidateUsers(taskSummary.getCandidateUsers())
                .candidateGroups(taskSummary.getCandidateGroups())
                .build();
    }

    private WorkflowTaskSummaryResponse toTaskSummary(Task task, String taskStatus) {
        Map<String, Object> variables = safeVariables(task.getProcessInstanceId());
        List<IdentityLink> identityLinks = flowableTaskGateway.getIdentityLinksForTask(task.getId());
        return WorkflowTaskSummaryResponse.builder()
                .taskId(task.getId())
                .taskName(task.getName())
                .taskDefinitionKey(task.getTaskDefinitionKey())
                .processInstanceId(task.getProcessInstanceId())
                .processDefinitionId(task.getProcessDefinitionId())
                .processDefinitionKey(task.getProcessDefinitionId() == null ? null : task.getProcessDefinitionId().split(":")[0])
                .businessKey(asString(variables.get("businessKey")))
                .assignee(task.getAssignee())
                .candidateUsers(identityLinks.stream().filter(link -> "candidate".equals(link.getType()) && link.getUserId() != null).map(IdentityLink::getUserId).distinct().toList())
                .candidateGroups(identityLinks.stream().filter(link -> "candidate".equals(link.getType()) && link.getGroupId() != null).map(IdentityLink::getGroupId).distinct().toList())
                .taskStatus(taskStatus)
                .starterId(asString(variables.get("starterId")))
                .starterDeptId(asString(variables.get("starterDeptId")))
                .formTitle(asString(variables.get("formTitle")))
                .createdAt(toDateTime(task.getCreateTime()))
                .completedAt(null)
                .build();
    }

    private WorkflowTaskSummaryResponse toHistoricTaskSummary(HistoricTaskInstance task) {
        HistoricProcessInstance processInstance = flowableTaskGateway.getHistoricProcessInstance(task.getProcessInstanceId());
        Map<String, Object> historicVariables = processInstance == null ? Map.of() : processInstance.getProcessVariables();
        return WorkflowTaskSummaryResponse.builder()
                .taskId(task.getId())
                .taskName(task.getName())
                .taskDefinitionKey(task.getTaskDefinitionKey())
                .processInstanceId(task.getProcessInstanceId())
                .processDefinitionId(task.getProcessDefinitionId())
                .processDefinitionKey(task.getProcessDefinitionId() == null ? null : task.getProcessDefinitionId().split(":")[0])
                .businessKey(processInstance == null ? null : processInstance.getBusinessKey())
                .assignee(task.getAssignee())
                .candidateUsers(List.of())
                .candidateGroups(List.of())
                .taskStatus("COMPLETED")
                .starterId(asString(historicVariables.get("starterId")))
                .starterDeptId(asString(historicVariables.get("starterDeptId")))
                .formTitle(asString(historicVariables.get("formTitle")))
                .createdAt(toDateTime(task.getCreateTime()))
                .completedAt(toDateTime(task.getEndTime()))
                .build();
    }

    private WorkflowInstanceSummaryResponse toInstanceSummary(HistoricProcessInstance instance) {
        Map<String, Object> historicVariables = instance.getProcessVariables() == null ? Map.of() : instance.getProcessVariables();
        return WorkflowInstanceSummaryResponse.builder()
                .processInstanceId(instance.getId())
                .processDefinitionId(instance.getProcessDefinitionId())
                .processDefinitionKey(instance.getProcessDefinitionKey())
                .businessKey(instance.getBusinessKey())
                .starterId(asString(historicVariables.get("starterId")))
                .starterDeptId(asString(historicVariables.get("starterDeptId")))
                .formTitle(asString(historicVariables.get("formTitle")))
                .status(instance.getEndTime() == null ? "RUNNING" : "FINISHED")
                .startTime(toDateTime(instance.getStartTime()))
                .endTime(toDateTime(instance.getEndTime()))
                .build();
    }

    private Task requireTask(String taskId) {
        Task task = flowableTaskGateway.getRuntimeTask(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在或已结束: " + taskId);
        }
        return task;
    }

    private Map<String, Object> safeVariables(String processInstanceId) {
        try {
            return flowableTaskGateway.getProcessVariables(processInstanceId);
        } catch (Exception ex) {
            return new LinkedHashMap<>();
        }
    }

    private Map<String, Object> safeHistoricVariables(String processInstanceId) {
        HistoricProcessInstance instance = flowableTaskGateway.getHistoricProcessInstance(processInstanceId);
        if (instance == null || instance.getProcessVariables() == null) {
            return Map.of();
        }
        return instance.getProcessVariables();
    }

    private String resolveUser(String userId) {
        return userId == null || userId.isBlank() ? CurrentUser.USER_ID : userId;
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private LocalDateTime toDateTime(java.util.Date value) {
        if (value == null) {
            return null;
        }
        return LocalDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault());
    }

    public record WorkflowTaskClaimedEvent(String taskId, String processInstanceId, String taskDefinitionKey, String userId) {
    }

    public record WorkflowTaskCompletedEvent(String taskId, String processInstanceId, String taskDefinitionKey, String userId) {
    }
}
