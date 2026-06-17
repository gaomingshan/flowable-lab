package com.flowable.lab.rest.controller;

import com.flowable.lab.common.dto.HistoricProcessInstanceDTO;
import com.flowable.lab.common.dto.JobDTO;
import com.flowable.lab.common.response.Result;
import com.flowable.lab.rest.mapper.MonitorMapper;
import com.flowable.lab.service.monitor.MonitorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/monitor")
public class MonitorController {

    private final MonitorService monitorService;
    private final MonitorMapper mapper;

    public MonitorController(MonitorService monitorService, MonitorMapper mapper) {
        this.monitorService = monitorService;
        this.mapper = mapper;
    }

    @GetMapping("/history")
    public Result<List<HistoricProcessInstanceDTO>> history() {
        return Result.success(mapper.toHistoricDtoList(monitorService.listHistory()));
    }

    @GetMapping("/statistics")
    public Result<Map<String, Long>> statistics() {
        return Result.success(monitorService.getStatistics());
    }

    @GetMapping("/jobs")
    public Result<List<JobDTO>> jobs() {
        return Result.success(mapper.toJobDtoList(monitorService.listJobs()));
    }

    @PostMapping("/jobs/{id}/execute")
    public Result<Void> executeJob(@PathVariable String id) {
        monitorService.executeJob(id);
        return Result.success();
    }

    @DeleteMapping("/jobs/{id}")
    public Result<Void> deleteJob(@PathVariable String id) {
        monitorService.deleteJob(id);
        return Result.success();
    }
}
