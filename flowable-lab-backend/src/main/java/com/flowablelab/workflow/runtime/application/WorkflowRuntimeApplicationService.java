package com.flowablelab.workflow.runtime.application;

import com.flowablelab.shared.api.CurrentUser;
import com.flowablelab.workflow.definition.domain.entity.WfDefinitionVersionEntity;
import com.flowablelab.workflow.definition.infrastructure.mapper.WfDefinitionVersionMapper;
import com.flowablelab.workflow.flowable.application.WorkflowDeploymentApplicationService;
import com.flowablelab.workflow.runtime.api.dto.WorkflowInstanceStartResponse;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WorkflowRuntimeApplicationService {

    private final RuntimeService runtimeService;
    private final WorkflowDeploymentApplicationService workflowDeploymentApplicationService;
    private final WfDefinitionVersionMapper versionMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    public WorkflowInstanceStartResponse startWorkflow(String definitionId,
                                                       String businessKey,
                                                       String title,
                                                       Map<String, Object> variables) {
        Integer releasedVersionNo = workflowDeploymentApplicationService.resolveReleasedVersionNo(definitionId);
        String processDefinitionKey = workflowDeploymentApplicationService.resolveProcessDefinitionKey(definitionId, releasedVersionNo);
        WfDefinitionVersionEntity version = versionMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WfDefinitionVersionEntity>()
                        .eq(WfDefinitionVersionEntity::getDefinitionId, definitionId)
                        .eq(WfDefinitionVersionEntity::getVersionNo, releasedVersionNo)
                        .last("limit 1")
        );
        if (version == null || version.getProcessDefinitionId() == null || version.getProcessDefinitionId().isBlank()) {
            throw new IllegalArgumentException("流程定义尚未发布，无法启动: " + definitionId);
        }

        Map<String, Object> runtimeVariables = new LinkedHashMap<>();
        if (variables != null) {
            runtimeVariables.putAll(variables);
        }
        runtimeVariables.put("starterId", CurrentUser.USER_ID);
        runtimeVariables.put("starterDeptId", CurrentUser.DEPT_ID);
        if (title != null && !title.isBlank()) {
            runtimeVariables.put("formTitle", title);
        }

        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionId(version.getProcessDefinitionId())
                .businessKey(businessKey)
                .variables(runtimeVariables)
                .start();

        applicationEventPublisher.publishEvent(new WorkflowInstanceStartedEvent(
                processInstance.getProcessInstanceId(),
                version.getProcessDefinitionId(),
                processDefinitionKey,
                businessKey,
                CurrentUser.USER_ID,
                CurrentUser.DEPT_ID,
                title
        ));

        return WorkflowInstanceStartResponse.builder()
                .instanceId(processInstance.getProcessInstanceId())
                .engineProcessInstanceId(processInstance.getProcessInstanceId())
                .definitionId(definitionId)
                .versionNo(releasedVersionNo)
                .businessKey(businessKey)
                .title(title)
                .status("running")
                .processDefinitionId(version.getProcessDefinitionId())
                .processDefinitionKey(processDefinitionKey)
                .build();
    }

    public record WorkflowInstanceStartedEvent(String processInstanceId,
                                               String processDefinitionId,
                                               String processDefinitionKey,
                                               String businessKey,
                                               String starterId,
                                               String starterDeptId,
                                               String formTitle) {
    }
}
