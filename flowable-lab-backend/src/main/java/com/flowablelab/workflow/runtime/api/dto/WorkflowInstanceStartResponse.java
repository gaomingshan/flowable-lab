package com.flowablelab.workflow.runtime.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorkflowInstanceStartResponse {

    private final String instanceId;
    private final String engineProcessInstanceId;
    private final String definitionId;
    private final Integer versionNo;
    private final String businessKey;
    private final String title;
    private final String status;
    private final String processDefinitionId;
    private final String processDefinitionKey;
}
