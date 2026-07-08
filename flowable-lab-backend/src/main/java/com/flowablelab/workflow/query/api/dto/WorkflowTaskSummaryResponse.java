package com.flowablelab.workflow.query.api.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class WorkflowTaskSummaryResponse {

    private final String taskId;
    private final String taskName;
    private final String taskDefinitionKey;
    private final String processInstanceId;
    private final String processDefinitionId;
    private final String processDefinitionKey;
    private final String businessKey;
    private final String assignee;
    private final List<String> candidateUsers;
    private final List<String> candidateGroups;
    private final String taskStatus;
    private final String starterId;
    private final String starterDeptId;
    private final String formTitle;
    private final LocalDateTime createdAt;
    private final LocalDateTime completedAt;
}
