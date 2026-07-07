package com.flowablelab.workflow.version.api;

import com.flowablelab.shared.api.ApiResponse;
import com.flowablelab.workflow.definition.api.WorkflowDefinitionFacade;
import com.flowablelab.workflow.definition.api.dto.WorkflowVersionResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequestMapping("/api/platform/workflow-definitions/{definitionId}/versions")
@RequiredArgsConstructor
public class WorkflowVersionController {

    private final WorkflowDefinitionFacade workflowDefinitionFacade;

    @PostMapping
    public ApiResponse<WorkflowVersionResponse> createDraftVersion(@PathVariable String definitionId,
                                                                   @RequestBody CreateVersionRequest request) {
        return ApiResponse.success(workflowDefinitionFacade.createDraftVersion(definitionId, request.getChangeNote()));
    }

    @GetMapping
    public ApiResponse<List<WorkflowVersionResponse>> listVersions(@PathVariable String definitionId) {
        return ApiResponse.success(workflowDefinitionFacade.listVersions(definitionId));
    }

    @Getter
    @Setter
    public static class CreateVersionRequest {
        @NotBlank
        private String changeNote;
    }
}
