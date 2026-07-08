package com.flowablelab.workflow.query.api.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class WorkflowInstanceSummaryResponse {

    private final String processInstanceId;
    private final String processDefinitionId;
    private final String processDefinitionKey;
    private final String businessKey;
    private final String starterId;
    private final String starterDeptId;
    private final String formTitle;
    private final String status;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
}
