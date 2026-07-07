package com.flowablelab.workflow.definition.api;

import com.flowablelab.shared.api.ApiResponse;
import com.flowablelab.workflow.definition.api.dto.WorkflowDefinitionRequest;
import com.flowablelab.workflow.definition.api.dto.WorkflowDefinitionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/platform/workflow-definitions")
@RequiredArgsConstructor
public class WorkflowDefinitionController {

    private final WorkflowDefinitionFacade workflowDefinitionFacade;

    @PostMapping
    public ApiResponse<WorkflowDefinitionResponse> createDefinition(@Valid @RequestBody WorkflowDefinitionRequest request) {
        return ApiResponse.success(workflowDefinitionFacade.createDefinition(request));
    }

    @GetMapping
    public ApiResponse<List<WorkflowDefinitionResponse>> listDefinitions() {
        return ApiResponse.success(workflowDefinitionFacade.listDefinitions());
    }

    @GetMapping("/{definitionId}")
    public ApiResponse<WorkflowDefinitionResponse> getDefinition(@PathVariable String definitionId) {
        return ApiResponse.success(workflowDefinitionFacade.getDefinition(definitionId));
    }

    @PutMapping("/{definitionId}")
    public ApiResponse<WorkflowDefinitionResponse> updateDefinition(@PathVariable String definitionId,
                                                                    @Valid @RequestBody WorkflowDefinitionRequest request) {
        return ApiResponse.success(workflowDefinitionFacade.updateDefinition(definitionId, request));
    }
}
