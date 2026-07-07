package com.flowablelab.workflow.definition.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorkflowReleaseResponse {

    private final String releaseId;
    private final String definitionId;
    private final Integer versionNo;
    private final String releaseComment;
    private final String releaseStatus;
    private final String deploymentId;
    private final String processDefinitionId;
}
