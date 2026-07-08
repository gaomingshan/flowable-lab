package com.flowablelab.workflow.runtime.api;

import com.flowablelab.shared.api.ApiResponse;
import com.flowablelab.workflow.runtime.api.dto.ClaimTaskRequest;
import com.flowablelab.workflow.runtime.api.dto.CompleteTaskRequest;
import com.flowablelab.workflow.runtime.api.dto.WorkflowInstanceStartResponse;
import com.flowablelab.workflow.runtime.api.dto.WorkflowTaskActionResponse;
import com.flowablelab.workflow.runtime.application.WorkflowRuntimeApplicationService;
import com.flowablelab.workflow.runtime.application.WorkflowTaskApplicationService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/platform/workflow-instances")
@RequiredArgsConstructor
public class WorkflowInstanceController {

    private final WorkflowRuntimeApplicationService workflowRuntimeApplicationService;
    private final WorkflowTaskApplicationService workflowTaskApplicationService;

    @PostMapping
    public ApiResponse<WorkflowInstanceStartResponse> startWorkflow(@RequestBody StartWorkflowRequest request) {
        return ApiResponse.success(workflowRuntimeApplicationService.startWorkflow(
                request.getDefinitionId(),
                request.getBusinessKey(),
                request.getTitle(),
                request.getVariables()
        ));
    }

    @PostMapping("/claim")
    public ApiResponse<WorkflowTaskActionResponse> claimTask(@Valid @RequestBody ClaimTaskRequest request) {
        return ApiResponse.success(workflowTaskApplicationService.claimTask(request.getTaskId(), request.getUserId()));
    }

    @PostMapping("/complete")
    public ApiResponse<WorkflowTaskActionResponse> completeTask(@Valid @RequestBody CompleteTaskRequest request) {
        return ApiResponse.success(workflowTaskApplicationService.completeTask(request.getTaskId(), request.getUserId(), request.getVariables()));
    }

    @Getter
    @Setter
    public static class StartWorkflowRequest {
        private String definitionId;
        private String businessKey;
        private String title;
        private String starterId;
        private String starterDeptId;
        private Map<String, Object> variables;
    }
}
