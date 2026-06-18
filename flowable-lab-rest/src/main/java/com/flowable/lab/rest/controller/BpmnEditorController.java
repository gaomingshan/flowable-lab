package com.flowable.lab.rest.controller;

import com.flowable.lab.common.response.Result;
import com.flowable.lab.converter.model.ProcessJson;
import com.flowable.lab.service.bpmn.BpmnEditorService;
import org.flowable.engine.repository.Deployment;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bpmn/editor")
public class BpmnEditorController {

    private final BpmnEditorService editorService;

    public BpmnEditorController(BpmnEditorService editorService) {
        this.editorService = editorService;
    }

    @GetMapping("/{definitionId}")
    public Result<ProcessJson> load(@PathVariable String definitionId) {
        ProcessJson processJson = editorService.loadProcess(definitionId);
        if (processJson == null) {
            return Result.error(404, "流程定义不存在: " + definitionId);
        }
        return Result.success(processJson);
    }

    @PostMapping("/deploy")
    public Result<Map<String, Object>> deploy(@RequestBody ProcessJson body) {
        String name = body.getProcess() != null ? body.getProcess().getName() : "未命名流程";
        Deployment deployment = editorService.deployProcess(body, name);
        return Result.success(Map.of(
                "deploymentId", deployment.getId(),
                "deployTime", deployment.getDeploymentTime()
        ));
    }
}
