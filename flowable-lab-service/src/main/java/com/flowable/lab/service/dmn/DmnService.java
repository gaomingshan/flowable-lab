package com.flowable.lab.service.dmn;

import org.flowable.dmn.api.DmnDecisionService;
import org.flowable.dmn.api.DmnDeployment;
import org.flowable.dmn.api.DmnRepositoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DmnService {

    private final DmnRepositoryService dmnRepositoryService;
    private final DmnDecisionService dmnDecisionService;

    public DmnService(DmnRepositoryService dmnRepositoryService,
                      DmnDecisionService dmnDecisionService) {
        this.dmnRepositoryService = dmnRepositoryService;
        this.dmnDecisionService = dmnDecisionService;
    }

    public DmnDeployment deploy(String name, String dmnContent) {
        return dmnRepositoryService.createDeployment()
                .name(name)
                .addString(name + ".dmn", dmnContent)
                .deploy();
    }

    public DmnDeployment deployByClasspath(String name, String classpathResource) {
        return dmnRepositoryService.createDeployment()
                .name(name)
                .addClasspathResource(classpathResource)
                .deploy();
    }

    public List<org.flowable.dmn.api.DmnDecision> listDecisions() {
        return dmnRepositoryService.createDecisionQuery()
                .latestVersion()
                .list();
    }

    public Map<String, Object> evaluateDecision(String decisionKey, Map<String, Object> variables) {
        List<Map<String, Object>> result = dmnDecisionService.createExecuteDecisionBuilder()
                .decisionKey(decisionKey)
                .variables(variables)
                .executeDecision();
        if (result == null || result.isEmpty()) {
            return Map.of();
        }
        return result.get(0);
    }

    public void deleteDeployment(String deploymentId) {
        dmnRepositoryService.deleteDeployment(deploymentId);
    }
}
