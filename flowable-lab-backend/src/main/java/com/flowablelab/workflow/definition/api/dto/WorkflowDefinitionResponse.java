package com.flowablelab.workflow.definition.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorkflowDefinitionResponse {

    private final String id;
    private final String definitionKey;
    private final String definitionName;
    private final String categoryCode;
    private final String description;
    private final String status;
    private final Integer latestDraftVersion;
    private final Integer latestReleasedVersion;
}
