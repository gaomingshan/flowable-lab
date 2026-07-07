package com.flowablelab.workflow.definition.api.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class WorkflowGraphResponse {

    private final String definitionId;
    private final Integer versionNo;
    private final Map<String, Object> graph;
    private final Integer syncedNodes;
    private final Integer syncedEdges;
}
