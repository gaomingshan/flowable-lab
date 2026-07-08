package com.flowablelab.workflow.definition.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowablelab.shared.api.CurrentUser;
import com.flowablelab.workflow.definition.api.WorkflowDefinitionFacade;
import com.flowablelab.workflow.definition.api.dto.WorkflowDefinitionRequest;
import com.flowablelab.workflow.definition.api.dto.WorkflowDefinitionResponse;
import com.flowablelab.workflow.definition.api.dto.WorkflowEdgeSkeletonResponse;
import com.flowablelab.workflow.definition.api.dto.WorkflowGraphResponse;
import com.flowablelab.workflow.definition.api.dto.WorkflowNodeSkeletonResponse;
import com.flowablelab.workflow.definition.api.dto.WorkflowReleaseResponse;
import com.flowablelab.workflow.definition.api.dto.WorkflowVersionResponse;
import com.flowablelab.workflow.definition.domain.entity.WfDefinitionEntity;
import com.flowablelab.workflow.definition.domain.entity.WfDefinitionVersionEntity;
import com.flowablelab.workflow.definition.domain.entity.WfEdgeEntity;
import com.flowablelab.workflow.definition.domain.entity.WfNodeEntity;
import com.flowablelab.workflow.definition.infrastructure.mapper.WfDefinitionMapper;
import com.flowablelab.workflow.definition.infrastructure.mapper.WfDefinitionVersionMapper;
import com.flowablelab.workflow.definition.infrastructure.mapper.WfEdgeMapper;
import com.flowablelab.workflow.definition.infrastructure.mapper.WfNodeMapper;
import com.flowablelab.workflow.flowable.application.WorkflowDeploymentApplicationService;
import com.flowablelab.workflow.query.api.dto.LaunchableWorkflowResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowDefinitionApplicationService implements WorkflowDefinitionFacade {

    private static final TypeReference<LinkedHashMap<String, Object>> GRAPH_TYPE = new TypeReference<>() { };

    private final WfDefinitionMapper definitionMapper;
    private final WfDefinitionVersionMapper versionMapper;
    private final WfNodeMapper nodeMapper;
    private final WfEdgeMapper edgeMapper;
    private final ObjectMapper objectMapper;
    private final WorkflowDeploymentApplicationService workflowDeploymentApplicationService;

    @Override
    @Transactional
    public WorkflowDefinitionResponse createDefinition(WorkflowDefinitionRequest request) {
        validateDefinitionKeyUnique(request.getDefinitionKey(), null);

        LocalDateTime now = LocalDateTime.now();
        WfDefinitionEntity definition = new WfDefinitionEntity();
        definition.setId(newId());
        definition.setDefinitionKey(request.getDefinitionKey());
        definition.setDefinitionName(request.getDefinitionName());
        definition.setCategoryCode(request.getCategoryCode());
        definition.setDescription(request.getDescription());
        definition.setStatus("draft");
        definition.setLatestDraftVersion(1);
        definition.setLatestReleasedVersion(0);
        definition.setCreatedBy(CurrentUser.USER_ID);
        definition.setUpdatedBy(CurrentUser.USER_ID);
        definition.setCreatedAt(now);
        definition.setUpdatedAt(now);
        definitionMapper.insert(definition);

        WfDefinitionVersionEntity version = new WfDefinitionVersionEntity();
        version.setId(newId());
        version.setDefinitionId(definition.getId());
        version.setVersionNo(1);
        version.setChangeNote("初始化版本");
        version.setStatus("draft");
        version.setGraphJson(writeGraph(defaultGraph()));
        version.setCreatedBy(CurrentUser.USER_ID);
        version.setUpdatedBy(CurrentUser.USER_ID);
        version.setCreatedAt(now);
        version.setUpdatedAt(now);
        versionMapper.insert(version);

        return toDefinitionResponse(definition);
    }

    @Override
    public List<WorkflowDefinitionResponse> listDefinitions() {
        return definitionMapper.selectList(new LambdaQueryWrapper<WfDefinitionEntity>()
                        .orderByDesc(WfDefinitionEntity::getUpdatedAt))
                .stream()
                .map(this::toDefinitionResponse)
                .toList();
    }

    @Override
    public WorkflowDefinitionResponse getDefinition(String definitionId) {
        return toDefinitionResponse(requireDefinition(definitionId));
    }

    @Override
    @Transactional
    public WorkflowDefinitionResponse updateDefinition(String definitionId, WorkflowDefinitionRequest request) {
        WfDefinitionEntity definition = requireDefinition(definitionId);
        validateDefinitionKeyUnique(request.getDefinitionKey(), definitionId);
        definition.setDefinitionKey(request.getDefinitionKey());
        definition.setDefinitionName(request.getDefinitionName());
        definition.setCategoryCode(request.getCategoryCode());
        definition.setDescription(request.getDescription());
        definition.setUpdatedBy(CurrentUser.USER_ID);
        definition.setUpdatedAt(LocalDateTime.now());
        definitionMapper.updateById(definition);
        return toDefinitionResponse(definition);
    }

    @Transactional
    public WorkflowVersionResponse createDraftVersion(String definitionId, String changeNote) {
        WfDefinitionEntity definition = requireDefinition(definitionId);
        WfDefinitionVersionEntity latestVersion = requireVersion(definitionId, definition.getLatestDraftVersion());
        int nextVersionNo = definition.getLatestDraftVersion() + 1;
        LocalDateTime now = LocalDateTime.now();

        WfDefinitionVersionEntity newVersion = new WfDefinitionVersionEntity();
        newVersion.setId(newId());
        newVersion.setDefinitionId(definitionId);
        newVersion.setVersionNo(nextVersionNo);
        newVersion.setChangeNote(changeNote);
        newVersion.setStatus("draft");
        newVersion.setGraphJson(latestVersion.getGraphJson());
        newVersion.setCreatedBy(CurrentUser.USER_ID);
        newVersion.setUpdatedBy(CurrentUser.USER_ID);
        newVersion.setCreatedAt(now);
        newVersion.setUpdatedAt(now);
        versionMapper.insert(newVersion);

        cloneSkeletons(latestVersion.getId(), newVersion.getId());

        definition.setLatestDraftVersion(nextVersionNo);
        definition.setUpdatedBy(CurrentUser.USER_ID);
        definition.setUpdatedAt(now);
        definitionMapper.updateById(definition);
        return toVersionResponse(newVersion);
    }

    public List<WorkflowVersionResponse> listVersions(String definitionId) {
        requireDefinition(definitionId);
        return versionMapper.selectList(new LambdaQueryWrapper<WfDefinitionVersionEntity>()
                        .eq(WfDefinitionVersionEntity::getDefinitionId, definitionId)
                        .orderByDesc(WfDefinitionVersionEntity::getVersionNo))
                .stream()
                .map(this::toVersionResponse)
                .toList();
    }

    public WorkflowGraphResponse getGraph(String definitionId, Integer versionNo) {
        WfDefinitionVersionEntity version = requireVersion(definitionId, versionNo);
        Map<String, Object> graph = readGraph(version.getGraphJson());
        return WorkflowGraphResponse.builder()
                .definitionId(definitionId)
                .versionNo(versionNo)
                .graph(graph)
                .syncedNodes(nodeCount(version.getId()))
                .syncedEdges(edgeCount(version.getId()))
                .build();
    }

    @Transactional
    public WorkflowGraphResponse saveGraph(String definitionId, Integer versionNo, Map<String, Object> graph) {
        WfDefinitionVersionEntity version = requireVersion(definitionId, versionNo);
        WfDefinitionEntity definition = requireDefinition(definitionId);
        Map<String, Object> normalizedGraph = normalizeGraph(graph);

        version.setGraphJson(writeGraph(normalizedGraph));
        version.setUpdatedBy(CurrentUser.USER_ID);
        version.setUpdatedAt(LocalDateTime.now());
        versionMapper.updateById(version);

        syncSkeletons(version.getId(), normalizedGraph);

        definition.setUpdatedBy(CurrentUser.USER_ID);
        definition.setUpdatedAt(LocalDateTime.now());
        definitionMapper.updateById(definition);

        return WorkflowGraphResponse.builder()
                .definitionId(definitionId)
                .versionNo(versionNo)
                .graph(normalizedGraph)
                .syncedNodes(nodeCount(version.getId()))
                .syncedEdges(edgeCount(version.getId()))
                .build();
    }

    public List<WorkflowNodeSkeletonResponse> listNodeSkeletons(String definitionId, Integer versionNo) {
        WfDefinitionVersionEntity version = requireVersion(definitionId, versionNo);
        return nodeMapper.selectList(new LambdaQueryWrapper<WfNodeEntity>()
                        .eq(WfNodeEntity::getVersionId, version.getId())
                        .orderByAsc(WfNodeEntity::getSortOrder))
                .stream()
                .map(node -> WorkflowNodeSkeletonResponse.builder()
                        .id(node.getId())
                        .nodeKey(node.getNodeKey())
                        .nodeName(node.getNodeName())
                        .nodeType(node.getNodeType())
                        .incomingCount(node.getIncomingCount())
                        .outgoingCount(node.getOutgoingCount())
                        .sortOrder(node.getSortOrder())
                        .build())
                .toList();
    }

    public List<WorkflowEdgeSkeletonResponse> listEdgeSkeletons(String definitionId, Integer versionNo) {
        WfDefinitionVersionEntity version = requireVersion(definitionId, versionNo);
        return edgeMapper.selectList(new LambdaQueryWrapper<WfEdgeEntity>()
                        .eq(WfEdgeEntity::getVersionId, version.getId())
                        .orderByAsc(WfEdgeEntity::getSortOrder))
                .stream()
                .map(edge -> WorkflowEdgeSkeletonResponse.builder()
                        .id(edge.getId())
                        .edgeKey(edge.getEdgeKey())
                        .sourceNodeKey(edge.getSourceNodeKey())
                        .targetNodeKey(edge.getTargetNodeKey())
                        .edgeType(edge.getEdgeType())
                        .conditionExpression(edge.getConditionExpression())
                        .isDefault(Boolean.TRUE.equals(edge.getIsDefault()))
                        .sortOrder(edge.getSortOrder())
                        .build())
                .toList();
    }

    @Transactional
    public WorkflowReleaseResponse releaseVersion(String definitionId, Integer versionNo, String releaseComment) {
        WfDefinitionEntity definition = requireDefinition(definitionId);
        WfDefinitionVersionEntity version = requireVersion(definitionId, versionNo);
        LocalDateTime now = LocalDateTime.now();
        WorkflowDeploymentApplicationService.DeploymentResult deploymentResult = workflowDeploymentApplicationService.deploy(definitionId, versionNo);

        version.setStatus("released");
        version.setReleaseComment(releaseComment);
        version.setDeploymentId(deploymentResult.deploymentId());
        version.setProcessDefinitionId(deploymentResult.processDefinitionId());
        version.setReleasedAt(now);
        version.setUpdatedBy(CurrentUser.USER_ID);
        version.setUpdatedAt(now);
        versionMapper.updateById(version);

        definition.setStatus("active");
        definition.setLatestReleasedVersion(versionNo);
        definition.setUpdatedBy(CurrentUser.USER_ID);
        definition.setUpdatedAt(now);
        definitionMapper.updateById(definition);

        return WorkflowReleaseResponse.builder()
                .releaseId(version.getId())
                .definitionId(definitionId)
                .versionNo(versionNo)
                .releaseComment(releaseComment)
                .releaseStatus(version.getStatus())
                .deploymentId(version.getDeploymentId())
                .processDefinitionId(version.getProcessDefinitionId())
                .build();
    }

    public WorkflowReleaseResponse getRelease(String releaseId) {
        WfDefinitionVersionEntity version = versionMapper.selectById(releaseId);
        if (version == null) {
            throw new IllegalArgumentException("发布记录不存在: " + releaseId);
        }
        return WorkflowReleaseResponse.builder()
                .releaseId(version.getId())
                .definitionId(version.getDefinitionId())
                .versionNo(version.getVersionNo())
                .releaseComment(version.getReleaseComment())
                .releaseStatus(version.getStatus())
                .deploymentId(version.getDeploymentId())
                .processDefinitionId(version.getProcessDefinitionId())
                .build();
    }

    public List<LaunchableWorkflowResponse> listLaunchableWorkflows() {
        return definitionMapper.selectList(new LambdaQueryWrapper<WfDefinitionEntity>()
                        .gt(WfDefinitionEntity::getLatestReleasedVersion, 0)
                        .orderByDesc(WfDefinitionEntity::getUpdatedAt))
                .stream()
                .map(definition -> LaunchableWorkflowResponse.builder()
                        .definitionId(definition.getId())
                        .definitionKey(definition.getDefinitionKey())
                        .definitionName(definition.getDefinitionName())
                        .status(definition.getStatus())
                        .latestReleasedVersion(definition.getLatestReleasedVersion())
                        .build())
                .toList();
    }

    private void validateDefinitionKeyUnique(String definitionKey, String currentDefinitionId) {
        WfDefinitionEntity existing = definitionMapper.selectOne(new LambdaQueryWrapper<WfDefinitionEntity>()
                .eq(WfDefinitionEntity::getDefinitionKey, definitionKey)
                .last("limit 1"));
        if (existing != null && !existing.getId().equals(currentDefinitionId)) {
            throw new IllegalArgumentException("流程定义Key已存在: " + definitionKey);
        }
    }

    private WfDefinitionEntity requireDefinition(String definitionId) {
        WfDefinitionEntity definition = definitionMapper.selectById(definitionId);
        if (definition == null) {
            throw new IllegalArgumentException("流程定义不存在: " + definitionId);
        }
        return definition;
    }

    private WfDefinitionVersionEntity requireVersion(String definitionId, Integer versionNo) {
        WfDefinitionVersionEntity version = versionMapper.selectOne(new LambdaQueryWrapper<WfDefinitionVersionEntity>()
                .eq(WfDefinitionVersionEntity::getDefinitionId, definitionId)
                .eq(WfDefinitionVersionEntity::getVersionNo, versionNo)
                .last("limit 1"));
        if (version == null) {
            throw new IllegalArgumentException("流程版本不存在: " + definitionId + " / " + versionNo);
        }
        return version;
    }

    private WorkflowDefinitionResponse toDefinitionResponse(WfDefinitionEntity definition) {
        return WorkflowDefinitionResponse.builder()
                .id(definition.getId())
                .definitionKey(definition.getDefinitionKey())
                .definitionName(definition.getDefinitionName())
                .categoryCode(definition.getCategoryCode())
                .description(definition.getDescription())
                .status(definition.getStatus())
                .latestDraftVersion(definition.getLatestDraftVersion())
                .latestReleasedVersion(definition.getLatestReleasedVersion())
                .build();
    }

    private WorkflowVersionResponse toVersionResponse(WfDefinitionVersionEntity version) {
        return WorkflowVersionResponse.builder()
                .id(version.getId())
                .definitionId(version.getDefinitionId())
                .versionNo(version.getVersionNo())
                .changeNote(version.getChangeNote())
                .status(version.getStatus())
                .releaseComment(version.getReleaseComment())
                .deploymentId(version.getDeploymentId())
                .processDefinitionId(version.getProcessDefinitionId())
                .build();
    }

    private void cloneSkeletons(String sourceVersionId, String targetVersionId) {
        List<WfNodeEntity> nodes = nodeMapper.selectList(new LambdaQueryWrapper<WfNodeEntity>()
                .eq(WfNodeEntity::getVersionId, sourceVersionId));
        for (WfNodeEntity node : nodes) {
            WfNodeEntity cloned = new WfNodeEntity();
            cloned.setId(newId());
            cloned.setVersionId(targetVersionId);
            cloned.setNodeKey(node.getNodeKey());
            cloned.setNodeName(node.getNodeName());
            cloned.setNodeType(node.getNodeType());
            cloned.setIncomingCount(node.getIncomingCount());
            cloned.setOutgoingCount(node.getOutgoingCount());
            cloned.setSortOrder(node.getSortOrder());
            nodeMapper.insert(cloned);
        }

        List<WfEdgeEntity> edges = edgeMapper.selectList(new LambdaQueryWrapper<WfEdgeEntity>()
                .eq(WfEdgeEntity::getVersionId, sourceVersionId));
        for (WfEdgeEntity edge : edges) {
            WfEdgeEntity cloned = new WfEdgeEntity();
            cloned.setId(newId());
            cloned.setVersionId(targetVersionId);
            cloned.setEdgeKey(edge.getEdgeKey());
            cloned.setSourceNodeKey(edge.getSourceNodeKey());
            cloned.setTargetNodeKey(edge.getTargetNodeKey());
            cloned.setEdgeType(edge.getEdgeType());
            cloned.setConditionExpression(edge.getConditionExpression());
            cloned.setIsDefault(edge.getIsDefault());
            cloned.setSortOrder(edge.getSortOrder());
            edgeMapper.insert(cloned);
        }
    }

    private void syncSkeletons(String versionId, Map<String, Object> graph) {
        nodeMapper.delete(new LambdaUpdateWrapper<WfNodeEntity>().eq(WfNodeEntity::getVersionId, versionId));
        edgeMapper.delete(new LambdaUpdateWrapper<WfEdgeEntity>().eq(WfEdgeEntity::getVersionId, versionId));

        List<Map<String, Object>> nodes = extractMaps(graph.get("nodes"));
        List<Map<String, Object>> edges = extractMaps(graph.get("edges"));
        Map<String, Integer> incoming = new LinkedHashMap<>();
        Map<String, Integer> outgoing = new LinkedHashMap<>();

        for (Map<String, Object> edge : edges) {
            String sourceNodeKey = asString(edge.get("sourceNodeKey"), asString(edge.get("source"), ""));
            String targetNodeKey = asString(edge.get("targetNodeKey"), asString(edge.get("target"), ""));
            if (!sourceNodeKey.isBlank()) {
                outgoing.merge(sourceNodeKey, 1, Integer::sum);
            }
            if (!targetNodeKey.isBlank()) {
                incoming.merge(targetNodeKey, 1, Integer::sum);
            }
        }

        for (int i = 0; i < nodes.size(); i++) {
            Map<String, Object> node = nodes.get(i);
            String nodeKey = resolveNodeKey(node, i);
            WfNodeEntity entity = new WfNodeEntity();
            entity.setId(newId());
            entity.setVersionId(versionId);
            entity.setNodeKey(nodeKey);
            entity.setNodeName(asString(node.get("nodeName"), asString(node.get("label"), nodeKey)));
            entity.setNodeType(asString(node.get("nodeType"), asString(node.get("type"), "userTask")));
            entity.setIncomingCount(incoming.getOrDefault(nodeKey, 0));
            entity.setOutgoingCount(outgoing.getOrDefault(nodeKey, 0));
            entity.setSortOrder(i + 1);
            nodeMapper.insert(entity);
        }

        for (int i = 0; i < edges.size(); i++) {
            Map<String, Object> edge = edges.get(i);
            WfEdgeEntity entity = new WfEdgeEntity();
            entity.setId(newId());
            entity.setVersionId(versionId);
            entity.setEdgeKey(resolveEdgeKey(edge, i));
            entity.setSourceNodeKey(asString(edge.get("sourceNodeKey"), asString(edge.get("source"), "")));
            entity.setTargetNodeKey(asString(edge.get("targetNodeKey"), asString(edge.get("target"), "")));
            entity.setEdgeType(asString(edge.get("edgeType"), asString(edge.get("type"), "sequenceFlow")));
            entity.setConditionExpression(asString(edge.get("conditionExpression"), null));
            entity.setIsDefault(asBoolean(edge.get("defaultBranch")));
            entity.setSortOrder(i + 1);
            edgeMapper.insert(entity);
        }
    }

    private int nodeCount(String versionId) {
        return Math.toIntExact(nodeMapper.selectCount(new LambdaQueryWrapper<WfNodeEntity>()
                .eq(WfNodeEntity::getVersionId, versionId)));
    }

    private int edgeCount(String versionId) {
        return Math.toIntExact(edgeMapper.selectCount(new LambdaQueryWrapper<WfEdgeEntity>()
                .eq(WfEdgeEntity::getVersionId, versionId)));
    }

    private Map<String, Object> normalizeGraph(Map<String, Object> graph) {
        Map<String, Object> normalized = new LinkedHashMap<>();
        Map<String, Object> source = graph == null ? defaultGraph() : graph;
        normalized.put("nodes", extractMaps(source.get("nodes")));
        normalized.put("edges", extractMaps(source.get("edges")));
        Object viewport = source.get("viewport");
        normalized.put("viewport", viewport instanceof Map<?, ?> ? viewport : defaultViewport());
        return normalized;
    }

    private Map<String, Object> defaultGraph() {
        Map<String, Object> graph = new LinkedHashMap<>();
        graph.put("nodes", new ArrayList<>());
        graph.put("edges", new ArrayList<>());
        graph.put("viewport", defaultViewport());
        return graph;
    }

    private Map<String, Object> defaultViewport() {
        Map<String, Object> viewport = new LinkedHashMap<>();
        viewport.put("x", 0);
        viewport.put("y", 0);
        viewport.put("zoom", 1);
        return viewport;
    }

    private Map<String, Object> readGraph(String graphJson) {
        try {
            return objectMapper.readValue(graphJson, GRAPH_TYPE);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("graph_json解析失败", ex);
        }
    }

    private String writeGraph(Map<String, Object> graph) {
        try {
            return objectMapper.writeValueAsString(graph);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("graph_json序列化失败", ex);
        }
    }

    private List<Map<String, Object>> extractMaps(Object value) {
        if (!(value instanceof List<?> list)) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                Map<String, Object> normalized = new LinkedHashMap<>();
                map.forEach((key, mapValue) -> normalized.put(String.valueOf(key), mapValue));
                result.add(normalized);
            }
        }
        return result;
    }

    private String resolveNodeKey(Map<String, Object> node, int index) {
        String nodeKey = asString(node.get("nodeKey"), asString(node.get("id"), "node-" + (index + 1)));
        if (nodeKey.isBlank()) {
            throw new IllegalArgumentException("节点缺少nodeKey/id");
        }
        return nodeKey;
    }

    private String resolveEdgeKey(Map<String, Object> edge, int index) {
        String edgeKey = asString(edge.get("edgeKey"), asString(edge.get("id"), "edge-" + (index + 1)));
        if (edgeKey.isBlank()) {
            throw new IllegalArgumentException("连线缺少edgeKey/id");
        }
        return edgeKey;
    }

    private String asString(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? defaultValue : text;
    }

    private Boolean asBoolean(Object value) {
        if (value == null) {
            return Boolean.FALSE;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private String newId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
