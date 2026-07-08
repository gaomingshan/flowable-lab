package com.flowablelab.workflow.flowable.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.flowablelab.workflow.definition.domain.entity.WfDefinitionEntity;
import com.flowablelab.workflow.definition.domain.entity.WfDefinitionVersionEntity;
import com.flowablelab.workflow.definition.domain.entity.WfEdgeEntity;
import com.flowablelab.workflow.definition.domain.entity.WfNodeEntity;
import com.flowablelab.workflow.definition.infrastructure.mapper.WfDefinitionMapper;
import com.flowablelab.workflow.definition.infrastructure.mapper.WfDefinitionVersionMapper;
import com.flowablelab.workflow.definition.infrastructure.mapper.WfEdgeMapper;
import com.flowablelab.workflow.definition.infrastructure.mapper.WfNodeMapper;
import lombok.RequiredArgsConstructor;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.EndEvent;
import org.flowable.bpmn.model.ExclusiveGateway;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.ParallelGateway;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WorkflowDeploymentApplicationService {

    private final RepositoryService repositoryService;
    private final WfDefinitionMapper definitionMapper;
    private final WfDefinitionVersionMapper versionMapper;
    private final WfNodeMapper nodeMapper;
    private final WfEdgeMapper edgeMapper;

    public DeploymentResult deploy(String definitionId, Integer versionNo) {
        WfDefinitionEntity definition = requireDefinition(definitionId);
        WfDefinitionVersionEntity version = requireVersion(definitionId, versionNo);
        BpmnModel bpmnModel = buildBpmnModel(definition, version);

        Deployment deployment = repositoryService.createDeployment()
                .name(definition.getDefinitionName() + "-V" + versionNo)
                .key(definition.getDefinitionKey() + "-v" + versionNo)
                .category(definition.getCategoryCode())
                .addBpmnModel(definition.getDefinitionKey() + ".bpmn20.xml", bpmnModel)
                .deploy();

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .processDefinitionKey(definition.getDefinitionKey())
                .latestVersion()
                .singleResult();

        if (processDefinition == null) {
            throw new IllegalStateException("Flowable流程定义生成失败: " + definition.getDefinitionKey());
        }

        return new DeploymentResult(
                deployment.getId(),
                processDefinition.getId(),
                processDefinition.getKey(),
                processDefinition.getVersion()
        );
    }

    public String resolveProcessDefinitionKey(String definitionId, Integer versionNo) {
        WfDefinitionEntity definition = requireDefinition(definitionId);
        WfDefinitionVersionEntity version = requireVersion(definitionId, versionNo);
        if (version.getProcessDefinitionId() == null || version.getProcessDefinitionId().isBlank()) {
            throw new IllegalArgumentException("流程版本尚未发布，无法启动: " + definitionId + " / " + versionNo);
        }
        return definition.getDefinitionKey();
    }

    public Integer resolveReleasedVersionNo(String definitionId) {
        WfDefinitionEntity definition = requireDefinition(definitionId);
        if (definition.getLatestReleasedVersion() == null || definition.getLatestReleasedVersion() <= 0) {
            throw new IllegalArgumentException("流程定义尚未发布，无法启动: " + definitionId);
        }
        return definition.getLatestReleasedVersion();
    }

    private BpmnModel buildBpmnModel(WfDefinitionEntity definition, WfDefinitionVersionEntity version) {
        List<WfNodeEntity> nodes = nodeMapper.selectList(new LambdaQueryWrapper<WfNodeEntity>()
                .eq(WfNodeEntity::getVersionId, version.getId())
                .orderByAsc(WfNodeEntity::getSortOrder));
        List<WfEdgeEntity> edges = edgeMapper.selectList(new LambdaQueryWrapper<WfEdgeEntity>()
                .eq(WfEdgeEntity::getVersionId, version.getId())
                .orderByAsc(WfEdgeEntity::getSortOrder));

        if (nodes.isEmpty()) {
            throw new IllegalArgumentException("流程版本没有节点，无法发布: " + definition.getDefinitionKey() + " / V" + version.getVersionNo());
        }

        Map<String, FlowElement> elementByNodeKey = new LinkedHashMap<>();
        Process process = new Process();
        process.setId(definition.getDefinitionKey());
        process.setName(definition.getDefinitionName());
        process.setExecutable(true);

        for (WfNodeEntity node : nodes) {
            FlowElement element = toFlowElement(node);
            process.addFlowElement(element);
            elementByNodeKey.put(node.getNodeKey(), element);
        }

        for (WfEdgeEntity edge : edges) {
            if (!elementByNodeKey.containsKey(edge.getSourceNodeKey()) || !elementByNodeKey.containsKey(edge.getTargetNodeKey())) {
                throw new IllegalArgumentException("连线端点不存在，无法发布: " + edge.getEdgeKey());
            }

            SequenceFlow sequenceFlow = new SequenceFlow();
            sequenceFlow.setId(edge.getEdgeKey());
            sequenceFlow.setSourceRef(edge.getSourceNodeKey());
            sequenceFlow.setTargetRef(edge.getTargetNodeKey());
            if (edge.getConditionExpression() != null && !edge.getConditionExpression().isBlank()) {
                sequenceFlow.setConditionExpression(edge.getConditionExpression().trim());
            }
            process.addFlowElement(sequenceFlow);
        }

        bindDefaultFlows(process, edges);
        validatePublishable(nodes, edges);

        BpmnModel bpmnModel = new BpmnModel();
        bpmnModel.addProcess(process);
        return bpmnModel;
    }

    private FlowElement toFlowElement(WfNodeEntity node) {
        return switch (node.getNodeType()) {
            case "startEvent" -> buildStartEvent(node);
            case "endEvent" -> buildEndEvent(node);
            case "userTask" -> buildUserTask(node);
            case "exclusiveGateway" -> buildExclusiveGateway(node);
            case "parallelGateway", "parallelJoin" -> buildParallelGateway(node);
            default -> throw new IllegalArgumentException("不支持的节点类型，无法发布到Flowable: " + node.getNodeType());
        };
    }

    private StartEvent buildStartEvent(WfNodeEntity node) {
        StartEvent event = new StartEvent();
        event.setId(node.getNodeKey());
        event.setName(node.getNodeName());
        return event;
    }

    private EndEvent buildEndEvent(WfNodeEntity node) {
        EndEvent event = new EndEvent();
        event.setId(node.getNodeKey());
        event.setName(node.getNodeName());
        return event;
    }

    private UserTask buildUserTask(WfNodeEntity node) {
        UserTask task = new UserTask();
        task.setId(node.getNodeKey());
        task.setName(node.getNodeName());
        return task;
    }

    private ExclusiveGateway buildExclusiveGateway(WfNodeEntity node) {
        ExclusiveGateway gateway = new ExclusiveGateway();
        gateway.setId(node.getNodeKey());
        gateway.setName(node.getNodeName());
        return gateway;
    }

    private ParallelGateway buildParallelGateway(WfNodeEntity node) {
        ParallelGateway gateway = new ParallelGateway();
        gateway.setId(node.getNodeKey());
        gateway.setName(node.getNodeName());
        return gateway;
    }

    private void bindDefaultFlows(Process process, List<WfEdgeEntity> edges) {
        Map<String, String> defaultFlowBySource = new LinkedHashMap<>();
        for (WfEdgeEntity edge : edges) {
            if (Boolean.TRUE.equals(edge.getIsDefault())) {
                defaultFlowBySource.put(edge.getSourceNodeKey(), edge.getEdgeKey());
            }
        }
        for (Map.Entry<String, String> entry : defaultFlowBySource.entrySet()) {
            FlowElement element = process.getFlowElement(entry.getKey());
            if (element instanceof ExclusiveGateway gateway) {
                gateway.setDefaultFlow(entry.getValue());
            }
        }
    }

    private void validatePublishable(List<WfNodeEntity> nodes, List<WfEdgeEntity> edges) {
        long startCount = nodes.stream().filter(node -> "startEvent".equals(node.getNodeType())).count();
        if (startCount != 1) {
            throw new IllegalArgumentException("发布要求恰好存在一个开始节点");
        }

        long endCount = nodes.stream().filter(node -> "endEvent".equals(node.getNodeType())).count();
        if (endCount < 1) {
            throw new IllegalArgumentException("发布要求至少存在一个结束节点");
        }

        Map<String, Long> defaultCountBySource = new LinkedHashMap<>();
        for (WfEdgeEntity edge : edges) {
            if (Boolean.TRUE.equals(edge.getIsDefault())) {
                defaultCountBySource.merge(edge.getSourceNodeKey(), 1L, Long::sum);
            }
        }
        boolean hasInvalidDefault = defaultCountBySource.values().stream().anyMatch(count -> count > 1);
        if (hasInvalidDefault) {
            throw new IllegalArgumentException("同一个节点只能配置一条默认分支");
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

    public record DeploymentResult(String deploymentId, String processDefinitionId, String processDefinitionKey, Integer processDefinitionVersion) {
    }
}
