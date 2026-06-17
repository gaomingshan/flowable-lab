package com.flowable.lab.service.monitor;

import org.flowable.engine.HistoryService;
import org.flowable.engine.ManagementService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.job.api.Job;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MonitorService {

    private final HistoryService historyService;
    private final ManagementService managementService;

    public MonitorService(HistoryService historyService, ManagementService managementService) {
        this.historyService = historyService;
        this.managementService = managementService;
    }

    public List<HistoricProcessInstance> listHistory() {
        return historyService.createHistoricProcessInstanceQuery()
                .orderByProcessInstanceStartTime().desc()
                .list();
    }

    public Map<String, Long> getStatistics() {
        Map<String, Long> stats = new HashMap<>();

        long runningCount = historyService.createHistoricProcessInstanceQuery()
                .unfinished()
                .count();
        long finishedCount = historyService.createHistoricProcessInstanceQuery()
                .finished()
                .count();

        stats.put("running", runningCount);
        stats.put("finished", finishedCount);
        stats.put("total", runningCount + finishedCount);

        return stats;
    }

    public List<Job> listJobs() {
        return managementService.createJobQuery().list();
    }

    public List<Job> listTimerJobs() {
        return managementService.createTimerJobQuery().list();
    }

    public List<Job> listSuspendedJobs() {
        return managementService.createSuspendedJobQuery().list();
    }

    public List<Job> listDeadLetterJobs() {
        return managementService.createDeadLetterJobQuery().list();
    }

    public void executeJob(String jobId) {
        managementService.executeJob(jobId);
    }

    public void deleteJob(String jobId) {
        managementService.deleteJob(jobId);
    }
}
