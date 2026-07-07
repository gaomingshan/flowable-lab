package com.flowablelab.workflow.definition.api;

import com.flowablelab.workflow.definition.api.dto.WorkflowDefinitionRequest;
import com.flowablelab.workflow.definition.api.dto.WorkflowDefinitionResponse;
import com.flowablelab.workflow.definition.api.dto.WorkflowEdgeSkeletonResponse;
import com.flowablelab.workflow.definition.api.dto.WorkflowGraphResponse;
import com.flowablelab.workflow.definition.api.dto.WorkflowNodeSkeletonResponse;
import com.flowablelab.workflow.definition.api.dto.WorkflowReleaseResponse;
import com.flowablelab.workflow.definition.api.dto.WorkflowVersionResponse;
import com.flowablelab.workflow.query.api.dto.LaunchableWorkflowResponse;

import java.util.List;
import java.util.Map;

public interface WorkflowDefinitionFacade {

    WorkflowDefinitionResponse createDefinition(WorkflowDefinitionRequest request);

    List<WorkflowDefinitionResponse> listDefinitions();

    WorkflowDefinitionResponse getDefinition(String definitionId);

    WorkflowDefinitionResponse updateDefinition(String definitionId, WorkflowDefinitionRequest request);

    WorkflowVersionResponse createDraftVersion(String definitionId, String changeNote);

    List<WorkflowVersionResponse> listVersions(String definitionId);

    WorkflowGraphResponse getGraph(String definitionId, Integer versionNo);

    WorkflowGraphResponse saveGraph(String definitionId, Integer versionNo, Map<String, Object> graph);

    List<WorkflowNodeSkeletonResponse> listNodeSkeletons(String definitionId, Integer versionNo);

    List<WorkflowEdgeSkeletonResponse> listEdgeSkeletons(String definitionId, Integer versionNo);

    WorkflowReleaseResponse releaseVersion(String definitionId, Integer versionNo, String releaseComment);

    WorkflowReleaseResponse getRelease(String releaseId);

    List<LaunchableWorkflowResponse> listLaunchableWorkflows();
}
