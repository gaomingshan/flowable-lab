package com.flowable.lab.rest.controller;

import com.flowable.lab.common.dto.DeploymentDTO;
import com.flowable.lab.common.dto.ProcessDefinitionDTO;
import com.flowable.lab.common.response.Result;
import com.flowable.lab.rest.mapper.BpmnMapper;
import com.flowable.lab.service.bpmn.BpmnDeployService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bpmn/definition")
public class BpmnDefinitionController {

    private final BpmnDeployService bpmnDeployService;
    private final BpmnMapper mapper;

    public BpmnDefinitionController(BpmnDeployService bpmnDeployService, BpmnMapper mapper) {
        this.bpmnDeployService = bpmnDeployService;
        this.mapper = mapper;
    }

    @GetMapping
    public Result<List<ProcessDefinitionDTO>> list() {
        return Result.success(mapper.toPdDtoList(bpmnDeployService.listProcessDefinitions()));
    }

    @GetMapping("/{id}")
    public Result<ProcessDefinitionDTO> getById(@PathVariable String id) {
        return Result.success(mapper.toDto(bpmnDeployService.getProcessDefinition(id)));
    }

    @GetMapping("/key/{key}")
    public Result<ProcessDefinitionDTO> getByKey(@PathVariable String key) {
        return Result.success(mapper.toDto(bpmnDeployService.getProcessDefinitionByKey(key)));
    }

    @GetMapping("/{id}/xml")
    public Result<String> getXml(@PathVariable String id) {
        String xml = bpmnDeployService.getProcessDefinitionXml(id);
        return Result.success(xml);
    }

    @PostMapping("/{id}/suspend")
    public Result<Void> suspend(@PathVariable String id) {
        bpmnDeployService.suspendProcessDefinition(id);
        return Result.success();
    }

    @PostMapping("/{id}/activate")
    public Result<Void> activate(@PathVariable String id) {
        bpmnDeployService.activateProcessDefinition(id);
        return Result.success();
    }

    @GetMapping("/deployments")
    public Result<List<DeploymentDTO>> listDeployments() {
        return Result.success(mapper.toDeployDtoList(bpmnDeployService.listDeployments()));
    }

    @DeleteMapping("/deployments")
    public Result<Void> deleteAllDeployments() {
        var deployments = bpmnDeployService.listDeployments();
        for (var dep : deployments) {
            bpmnDeployService.deleteDeployment(dep.getId());
        }
        return Result.success();
    }

    @DeleteMapping("/deployments/{id}")
    public Result<Void> deleteDeployment(@PathVariable String id) {
        bpmnDeployService.deleteDeployment(id);
        return Result.success();
    }

    @PostMapping("/deploy")
    public Result<Map<String, Object>> deploy(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String bpmnXml = body.get("bpmnXml");
        if (name == null || bpmnXml == null) {
            return Result.error(400, "name and bpmnXml required");
        }
        var deployment = bpmnDeployService.deploy(name, bpmnXml);
        List<ProcessDefinitionDTO> definitions = mapper.toPdDtoList(bpmnDeployService.listProcessDefinitions());
        return Result.success(Map.of(
            "deploymentId", deployment.getId(),
            "definitions", definitions
        ));
    }
}
