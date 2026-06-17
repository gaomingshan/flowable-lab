package com.flowable.lab.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricTaskInstanceDTO {
    private String id;
    private String name;
    private String description;
    private String assignee;
    private String processDefinitionId;
    private String processInstanceId;
    private Date startTime;
    private Date endTime;
    private Long durationInMillis;
    private String deleteReason;
    private String formKey;
    private String taskDefinitionKey;
    private String owner;
    private String category;
    private String tenantId;
    private int priority;
}
