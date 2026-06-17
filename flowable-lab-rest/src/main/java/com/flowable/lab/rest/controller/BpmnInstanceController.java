package com.flowable.lab.rest.controller;

import com.flowable.lab.common.dto.HistoricProcessInstanceDTO;
import com.flowable.lab.common.dto.ProcessInstanceDTO;
import com.flowable.lab.common.response.Result;
import com.flowable.lab.rest.mapper.BpmnMapper;
import com.flowable.lab.service.bpmn.BpmnInstanceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bpmn/instance")
public class BpmnInstanceController {

    private final BpmnInstanceService bpmnInstanceService;
    private final BpmnMapper mapper;

    public BpmnInstanceController(BpmnInstanceService bpmnInstanceService, BpmnMapper mapper) {
        this.bpmnInstanceService = bpmnInstanceService;
        this.mapper = mapper;
    }

    @PostMapping("/start/{processKey}")
    public Result<ProcessInstanceDTO> start(@PathVariable String processKey,
                                             @RequestBody(required = false) Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) {
            return Result.success(mapper.toDto(bpmnInstanceService.startProcessByKey(processKey)));
        }
        return Result.success(mapper.toDto(bpmnInstanceService.startProcessByKey(processKey, variables)));
    }

    @GetMapping
    public Result<?> list(@RequestParam(required = false) String processKey,
                          @RequestParam(defaultValue = "running") String status) {
        if ("history".equals(status)) {
            if (processKey != null) {
                return Result.success(mapper.toHistoricPiDtoList(bpmnInstanceService.listHistoricInstancesByKey(processKey)));
            }
            return Result.success(mapper.toHistoricPiDtoList(bpmnInstanceService.listHistoricInstances()));
        }
        if (processKey != null) {
            return Result.success(mapper.toPiDtoList(bpmnInstanceService.listRunningInstancesByKey(processKey)));
        }
        return Result.success(mapper.toPiDtoList(bpmnInstanceService.listRunningInstances()));
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable String id,
                             @RequestParam(defaultValue = "runtime") String type) {
        if ("history".equals(type)) {
            return Result.success(mapper.toHistoricDto(bpmnInstanceService.getHistoricInstance(id)));
        }
        return Result.success(mapper.toDto(bpmnInstanceService.getProcessInstance(id)));
    }

    @PostMapping("/{id}/suspend")
    public Result<Void> suspend(@PathVariable String id) {
        bpmnInstanceService.suspendInstance(id);
        return Result.success();
    }

    @PostMapping("/{id}/activate")
    public Result<Void> activate(@PathVariable String id) {
        bpmnInstanceService.activateInstance(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id, @RequestParam(defaultValue = "手动删除") String reason) {
        bpmnInstanceService.deleteInstance(id, reason);
        return Result.success();
    }

    @GetMapping("/{id}/variables")
    public Result<Map<String, Object>> getVariables(@PathVariable String id) {
        return Result.success(bpmnInstanceService.getVariables(id));
    }

    @PostMapping("/{id}/variables")
    public Result<Void> setVariable(@PathVariable String id, @RequestBody Map<String, Object> variables) {
        variables.forEach((k, v) -> bpmnInstanceService.setVariable(id, k, v));
        return Result.success();
    }
}
