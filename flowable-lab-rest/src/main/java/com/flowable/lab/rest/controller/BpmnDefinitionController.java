package com.flowable.lab.rest.controller;

import com.flowable.lab.common.dto.DeploymentDTO;
import com.flowable.lab.common.dto.ProcessDefinitionDTO;
import com.flowable.lab.common.response.Result;
import com.flowable.lab.rest.mapper.BpmnMapper;
import com.flowable.lab.service.bpmn.BpmnDeployService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @DeleteMapping("/deployments/{id}")
    public Result<Void> deleteDeployment(@PathVariable String id) {
        bpmnDeployService.deleteDeployment(id);
        return Result.success();
    }
}
