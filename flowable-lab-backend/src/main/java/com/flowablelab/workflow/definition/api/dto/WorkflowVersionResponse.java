package com.flowablelab.workflow.definition.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorkflowVersionResponse {

    private final String id;
    private final String definitionId;
    private final Integer versionNo;
    private final String changeNote;
    private final String status;
    private final String releaseComment;
    private final String deploymentId;
    private final String processDefinitionId;
}
