package com.flowablelab.workflow.release.api;

import com.flowablelab.shared.api.ApiResponse;
import com.flowablelab.workflow.definition.api.WorkflowDefinitionFacade;
import com.flowablelab.workflow.definition.api.dto.WorkflowReleaseResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WorkflowReleaseController {

    private final WorkflowDefinitionFacade workflowDefinitionFacade;

    @PostMapping("/api/platform/workflow-definitions/{definitionId}/versions/{versionNo}/release")
    public ApiResponse<WorkflowReleaseResponse> releaseVersion(@PathVariable String definitionId,
                                                               @PathVariable Integer versionNo,
                                                               @RequestBody ReleaseRequest request) {
        return ApiResponse.success(workflowDefinitionFacade.releaseVersion(definitionId, versionNo, request.getReleaseComment()));
    }

    @GetMapping("/api/platform/releases/{releaseId}")
    public ApiResponse<WorkflowReleaseResponse> getRelease(@PathVariable String releaseId) {
        return ApiResponse.success(workflowDefinitionFacade.getRelease(releaseId));
    }

    @Getter
    @Setter
    public static class ReleaseRequest {
        private String releaseComment;
    }
}
