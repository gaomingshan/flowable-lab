package com.flowablelab.workflow.runtime.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorkflowTaskActionResponse {

    private final String taskId;
    private final String action;
    private final String assignee;
    private final String processInstanceId;
    private final String status;
}
