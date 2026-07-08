package com.flowablelab.workflow.runtime.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class CompleteTaskRequest {

    @NotBlank
    private String taskId;

    private String userId;

    private Map<String, Object> variables;
}
