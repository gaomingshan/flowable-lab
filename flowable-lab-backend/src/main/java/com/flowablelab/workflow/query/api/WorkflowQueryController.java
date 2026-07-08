package com.flowablelab.workflow.query.api;

import com.flowablelab.shared.api.ApiResponse;
import com.flowablelab.workflow.definition.api.WorkflowDefinitionFacade;
import com.flowablelab.workflow.query.api.dto.LaunchableWorkflowResponse;
import com.flowablelab.workflow.query.api.dto.WorkflowInstanceSummaryResponse;
import com.flowablelab.workflow.query.api.dto.WorkflowTaskDetailResponse;
import com.flowablelab.workflow.query.api.dto.WorkflowTaskSummaryResponse;
import com.flowablelab.workflow.runtime.application.WorkflowTaskApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/platform")
@RequiredArgsConstructor
public class WorkflowQueryController {

    private final WorkflowDefinitionFacade workflowDefinitionFacade;
    private final WorkflowTaskApplicationService workflowTaskApplicationService;

    @GetMapping("/launchable-workflows")
    public ApiResponse<List<LaunchableWorkflowResponse>> listLaunchableWorkflows() {
        return ApiResponse.success(workflowDefinitionFacade.listLaunchableWorkflows());
    }

    @GetMapping("/query/unclaimed")
    public ApiResponse<List<WorkflowTaskSummaryResponse>> listUnclaimed(@RequestParam(required = false) String userId) {
        return ApiResponse.success(workflowTaskApplicationService.listUnclaimedTasks(userId));
    }

    @GetMapping("/query/todo")
    public ApiResponse<List<WorkflowTaskSummaryResponse>> listTodo(@RequestParam(required = false) String userId) {
        return ApiResponse.success(workflowTaskApplicationService.listTodoTasks(userId));
    }

    @GetMapping("/query/done")
    public ApiResponse<List<WorkflowTaskSummaryResponse>> listDone(@RequestParam(required = false) String userId) {
        return ApiResponse.success(workflowTaskApplicationService.listDoneTasks(userId));
    }

    @GetMapping("/query/tasks/{taskId}")
    public ApiResponse<WorkflowTaskDetailResponse> getTaskDetail(@PathVariable String taskId) {
        return ApiResponse.success(workflowTaskApplicationService.getTaskDetail(taskId));
    }

    @GetMapping("/query/initiated")
    public ApiResponse<List<WorkflowInstanceSummaryResponse>> listInitiated(@RequestParam(required = false) String userId) {
        return ApiResponse.success(workflowTaskApplicationService.listInitiatedInstances(userId));
    }

    @GetMapping("/query/instances/{instanceId}/summary")
    public ApiResponse<WorkflowInstanceSummaryResponse> getInstanceSummary(@PathVariable String instanceId) {
        return ApiResponse.success(workflowTaskApplicationService.getInstanceSummary(instanceId));
    }
}
