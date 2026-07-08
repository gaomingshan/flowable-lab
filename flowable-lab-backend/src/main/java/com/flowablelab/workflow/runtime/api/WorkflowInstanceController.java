package com.flowablelab.workflow.runtime.api;

import com.flowablelab.shared.api.ApiResponse;
import com.flowablelab.workflow.runtime.api.dto.WorkflowInstanceStartResponse;
import com.flowablelab.workflow.runtime.application.WorkflowRuntimeApplicationService;
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

    @PostMapping
    public ApiResponse<WorkflowInstanceStartResponse> startWorkflow(@RequestBody StartWorkflowRequest request) {
        return ApiResponse.success(workflowRuntimeApplicationService.startWorkflow(
                request.getDefinitionId(),
                request.getBusinessKey(),
                request.getTitle(),
                request.getVariables()
        ));
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
