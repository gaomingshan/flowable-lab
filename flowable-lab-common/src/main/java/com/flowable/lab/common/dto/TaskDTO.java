package com.flowable.lab.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {
    private String id;
    private String name;
    private String description;
    private String assignee;
    private String processDefinitionId;
    private String processInstanceId;
    private String executionId;
    private Date createTime;
    private String formKey;
    private String taskDefinitionKey;
    private String tenantId;
    private boolean suspended;
    private int priority;
    private String category;
}
