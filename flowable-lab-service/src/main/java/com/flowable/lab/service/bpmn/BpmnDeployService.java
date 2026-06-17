package com.flowable.lab.service.bpmn;

import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentBuilder;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BpmnDeployService {

    private final RepositoryService repositoryService;

    public BpmnDeployService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    public Deployment deploy(String name, String bpmnContent) {
        DeploymentBuilder builder = repositoryService.createDeployment()
                .name(name)
                .addString(name + ".bpmn20.xml", bpmnContent);
        return builder.deploy();
    }

    public Deployment deployByClasspath(String name, String classpathResource) {
        DeploymentBuilder builder = repositoryService.createDeployment()
                .name(name)
                .addClasspathResource(classpathResource);
        return builder.deploy();
    }

    public List<ProcessDefinition> listProcessDefinitions() {
        return repositoryService.createProcessDefinitionQuery()
                .latestVersion()
                .orderByProcessDefinitionKey().asc()
                .list();
    }

    public ProcessDefinition getProcessDefinition(String definitionId) {
        return repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(definitionId)
                .singleResult();
    }

    public ProcessDefinition getProcessDefinitionByKey(String key) {
        return repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(key)
                .latestVersion()
                .singleResult();
    }

    public void suspendProcessDefinition(String definitionId) {
        repositoryService.suspendProcessDefinitionById(definitionId);
    }

    public void activateProcessDefinition(String definitionId) {
        repositoryService.activateProcessDefinitionById(definitionId);
    }

    public List<Deployment> listDeployments() {
        return repositoryService.createDeploymentQuery()
                .orderByDeploymentTime().desc()
                .list();
    }

    public void deleteDeployment(String deploymentId) {
        repositoryService.deleteDeployment(deploymentId, true);
    }
}
