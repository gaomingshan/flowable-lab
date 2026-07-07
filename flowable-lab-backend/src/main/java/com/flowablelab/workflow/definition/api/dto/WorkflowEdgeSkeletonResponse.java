package com.flowablelab.workflow.definition.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorkflowEdgeSkeletonResponse {

    private final String id;
    private final String edgeKey;
    private final String sourceNodeKey;
    private final String targetNodeKey;
    private final String edgeType;
    private final String conditionExpression;
    private final Boolean isDefault;
    private final Integer sortOrder;
}
