package com.flowablelab.workflow.definition.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorkflowNodeSkeletonResponse {

    private final String id;
    private final String nodeKey;
    private final String nodeName;
    private final String nodeType;
    private final Integer incomingCount;
    private final Integer outgoingCount;
    private final Integer sortOrder;
}
