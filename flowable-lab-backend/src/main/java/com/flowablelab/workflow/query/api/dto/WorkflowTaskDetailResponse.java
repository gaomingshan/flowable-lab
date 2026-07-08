package com.flowablelab.workflow.query.api.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class WorkflowTaskDetailResponse {

    private final WorkflowTaskSummaryResponse task;
    private final WorkflowInstanceSummaryResponse instance;
    private final Map<String, Object> variables;
    private final List<String> candidateUsers;
    private final List<String> candidateGroups;
}
