package com.flowable.lab.rest.controller;

import com.flowable.lab.common.dto.HistoricTaskInstanceDTO;
import com.flowable.lab.common.dto.TaskDTO;
import com.flowable.lab.common.response.Result;
import com.flowable.lab.rest.mapper.BpmnMapper;
import com.flowable.lab.service.bpmn.BpmnTaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/task")
public class TaskController {

    private final BpmnTaskService bpmnTaskService;
    private final BpmnMapper mapper;

    public TaskController(BpmnTaskService bpmnTaskService, BpmnMapper mapper) {
        this.bpmnTaskService = bpmnTaskService;
        this.mapper = mapper;
    }

    @GetMapping("/todo")
    public Result<List<TaskDTO>> todo(@RequestParam(required = false) String assignee,
                                      @RequestParam(required = false) String groupId) {
        if (groupId != null) {
            return Result.success(mapper.toTaskDtoList(bpmnTaskService.listTodoTasksByGroup(groupId)));
        }
        if (assignee != null) {
            return Result.success(mapper.toTaskDtoList(bpmnTaskService.listTodoTasks(assignee)));
        }
        return Result.success(mapper.toTaskDtoList(bpmnTaskService.listUnassignedTasks()));
    }

    @GetMapping("/done")
    public Result<List<HistoricTaskInstanceDTO>> done(@RequestParam String assignee) {
        return Result.success(mapper.toHistoricTaskDtoList(bpmnTaskService.listDoneTasks(assignee)));
    }

    @GetMapping("/{id}")
    public Result<TaskDTO> getById(@PathVariable String id) {
        return Result.success(mapper.toDto(bpmnTaskService.getTask(id)));
    }

    @PostMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable String id,
                                 @RequestBody(required = false) Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) {
            bpmnTaskService.completeTask(id);
        } else {
            bpmnTaskService.completeTask(id, variables);
        }
        return Result.success();
    }

    @PostMapping("/{id}/claim")
    public Result<Void> claim(@PathVariable String id, @RequestParam String userId) {
        bpmnTaskService.claimTask(id, userId);
        return Result.success();
    }

    @PostMapping("/{id}/unclaim")
    public Result<Void> unclaim(@PathVariable String id) {
        bpmnTaskService.unclaimTask(id);
        return Result.success();
    }

    @PostMapping("/{id}/delegate")
    public Result<Void> delegate(@PathVariable String id, @RequestParam String userId) {
        bpmnTaskService.delegateTask(id, userId);
        return Result.success();
    }

    @PostMapping("/{id}/transfer")
    public Result<Void> transfer(@PathVariable String id, @RequestParam String userId) {
        bpmnTaskService.transferTask(id, userId);
        return Result.success();
    }

    @PostMapping("/{id}/resolve")
    public Result<Void> resolve(@PathVariable String id) {
        bpmnTaskService.resolveTask(id);
        return Result.success();
    }

    @PostMapping("/{id}/comment")
    public Result<Void> addComment(@PathVariable String id,
                                   @RequestParam String processInstanceId,
                                   @RequestParam String message) {
        bpmnTaskService.addComment(id, processInstanceId, message);
        return Result.success();
    }
}
