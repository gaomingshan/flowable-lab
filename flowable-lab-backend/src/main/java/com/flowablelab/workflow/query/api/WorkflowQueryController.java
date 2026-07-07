package com.flowablelab.workflow.query.api;

import com.flowablelab.shared.api.ApiResponse;
import com.flowablelab.workflow.definition.api.WorkflowDefinitionFacade;
import com.flowablelab.workflow.query.api.dto.LaunchableWorkflowResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/platform")
@RequiredArgsConstructor
public class WorkflowQueryController {

    private final WorkflowDefinitionFacade workflowDefinitionFacade;

    @GetMapping("/launchable-workflows")
    public ApiResponse<List<LaunchableWorkflowResponse>> listLaunchableWorkflows() {
        return ApiResponse.success(workflowDefinitionFacade.listLaunchableWorkflows());
    }

    @GetMapping("/query/todo")
    public ApiResponse<List<Map<String, Object>>> listTodo() {
        return ApiResponse.success(List.of());
    }

    @GetMapping("/query/done")
    public ApiResponse<List<Map<String, Object>>> listDone() {
        return ApiResponse.success(List.of());
    }

    @GetMapping("/query/initiated")
    public ApiResponse<List<Map<String, Object>>> listInitiated() {
        return ApiResponse.success(List.of());
    }

    @GetMapping("/query/instances/{instanceId}/summary")
    public ApiResponse<Map<String, Object>> getInstanceSummary(@PathVariable String instanceId) {
        return ApiResponse.success(Map.of(
                "instanceId", instanceId,
                "status", "running"
        ));
    }
}
