package com.flowablelab.workflow.graph.api;

import com.flowablelab.shared.api.ApiResponse;
import com.flowablelab.workflow.definition.api.WorkflowDefinitionFacade;
import com.flowablelab.workflow.definition.api.dto.WorkflowEdgeSkeletonResponse;
import com.flowablelab.workflow.definition.api.dto.WorkflowGraphResponse;
import com.flowablelab.workflow.definition.api.dto.WorkflowNodeSkeletonResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/platform/workflow-definitions/{definitionId}/versions/{versionNo}")
@RequiredArgsConstructor
public class WorkflowGraphController {

    private final WorkflowDefinitionFacade workflowDefinitionFacade;

    @GetMapping("/graph")
    public ApiResponse<WorkflowGraphResponse> getGraph(@PathVariable String definitionId, @PathVariable Integer versionNo) {
        return ApiResponse.success(workflowDefinitionFacade.getGraph(definitionId, versionNo));
    }

    @PutMapping("/graph")
    public ApiResponse<WorkflowGraphResponse> saveGraph(@PathVariable String definitionId,
                                                        @PathVariable Integer versionNo,
                                                        @RequestBody SaveGraphRequest request) {
        return ApiResponse.success(workflowDefinitionFacade.saveGraph(definitionId, versionNo, request.getGraph()));
    }

    @GetMapping("/nodes")
    public ApiResponse<List<WorkflowNodeSkeletonResponse>> listNodeSkeletons(@PathVariable String definitionId,
                                                                             @PathVariable Integer versionNo) {
        return ApiResponse.success(workflowDefinitionFacade.listNodeSkeletons(definitionId, versionNo));
    }

    @GetMapping("/edges")
    public ApiResponse<List<WorkflowEdgeSkeletonResponse>> listEdgeSkeletons(@PathVariable String definitionId,
                                                                             @PathVariable Integer versionNo) {
        return ApiResponse.success(workflowDefinitionFacade.listEdgeSkeletons(definitionId, versionNo));
    }

    @Getter
    @Setter
    public static class SaveGraphRequest {
        private Map<String, Object> graph;
    }
}
