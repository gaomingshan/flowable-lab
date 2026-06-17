package com.flowable.lab.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricProcessInstanceDTO {
    private String id;
    private String processDefinitionId;
    private String processDefinitionKey;
    private String processDefinitionName;
    private int processDefinitionVersion;
    private String businessKey;
    private String deploymentId;
    private Date startTime;
    private Date endTime;
    private Long durationInMillis;
    private String startUserId;
    private String startActivityId;
    private String deleteReason;
    private String name;
    private String description;
    private String tenantId;
    private boolean ended;
}
