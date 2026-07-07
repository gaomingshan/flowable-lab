package com.flowablelab.workflow.query.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LaunchableWorkflowResponse {

    private final String definitionId;
    private final String definitionKey;
    private final String definitionName;
    private final String status;
    private final Integer latestReleasedVersion;
}
