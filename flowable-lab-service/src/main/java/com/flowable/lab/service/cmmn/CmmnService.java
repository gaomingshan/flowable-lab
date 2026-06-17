package com.flowable.lab.service.cmmn;

import org.flowable.cmmn.api.CmmnHistoryService;
import org.flowable.cmmn.api.CmmnRepositoryService;
import org.flowable.cmmn.api.CmmnRuntimeService;
import org.flowable.cmmn.api.repository.CaseDefinition;
import org.flowable.cmmn.api.runtime.CaseInstance;
import org.flowable.cmmn.api.runtime.PlanItemInstance;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import java.util.Map;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CmmnService {

    private final CmmnRepositoryService cmmnRepositoryService;
    private final CmmnRuntimeService cmmnRuntimeService;
    private final CmmnHistoryService cmmnHistoryService;
    private final TaskService taskService;

    public CmmnService(CmmnRepositoryService cmmnRepositoryService,
                       CmmnRuntimeService cmmnRuntimeService,
                       CmmnHistoryService cmmnHistoryService,
                       TaskService taskService) {
        this.cmmnRepositoryService = cmmnRepositoryService;
        this.cmmnRuntimeService = cmmnRuntimeService;
        this.cmmnHistoryService = cmmnHistoryService;
        this.taskService = taskService;
    }

    public org.flowable.cmmn.api.repository.CmmnDeployment deploy(String name, String cmmnContent) {
        return cmmnRepositoryService.createDeployment()
                .name(name)
                .addString(name + ".cmmn", cmmnContent)
                .deploy();
    }

    public org.flowable.cmmn.api.repository.CmmnDeployment deployByClasspath(String name, String classpathResource) {
        return cmmnRepositoryService.createDeployment()
                .name(name)
                .addClasspathResource(classpathResource)
                .deploy();
    }

    public List<CaseDefinition> listCaseDefinitions() {
        return cmmnRepositoryService.createCaseDefinitionQuery()
                .latestVersion()
                .list();
    }

    public CaseInstance startCase(String caseKey) {
        return cmmnRuntimeService.createCaseInstanceBuilder()
                .caseDefinitionKey(caseKey)
                .start();
    }

    public CaseInstance startCase(String caseKey, Map<String, Object> variables) {
        return cmmnRuntimeService.createCaseInstanceBuilder()
                .caseDefinitionKey(caseKey)
                .variables(variables)
                .start();
    }

    public CaseInstance getCaseInstance(String caseInstanceId) {
        return cmmnRuntimeService.createCaseInstanceQuery()
                .caseInstanceId(caseInstanceId)
                .singleResult();
    }

    public List<CaseInstance> listRunningCases() {
        return cmmnRuntimeService.createCaseInstanceQuery()
                .orderByStartTime().desc()
                .list();
    }

    public void terminateCase(String caseInstanceId) {
        cmmnRuntimeService.terminateCaseInstance(caseInstanceId);
    }

    public List<PlanItemInstance> getPlanItemInstances(String caseInstanceId) {
        return cmmnRuntimeService.createPlanItemInstanceQuery()
                .caseInstanceId(caseInstanceId)
                .list();
    }

    public void triggerPlanItem(String planItemInstanceId) {
        cmmnRuntimeService.triggerPlanItemInstance(planItemInstanceId);
    }

    public List<Task> getActiveTasks(String caseInstanceId) {
        return taskService.createTaskQuery()
                .caseInstanceId(caseInstanceId)
                .active()
                .list();
    }
}
