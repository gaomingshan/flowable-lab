package com.flowablelab.workflow.runtime.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClaimTaskRequest {

    @NotBlank
    private String taskId;

    private String userId;
}
