package com.flowablelab.workflow.definition.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkflowDefinitionRequest {

    @NotBlank
    private String definitionKey;

    @NotBlank
    private String definitionName;

    @NotBlank
    private String categoryCode;

    private String description;
}
