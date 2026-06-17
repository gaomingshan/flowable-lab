package com.flowable.lab.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessInstanceDTO {
    private String id;
    private String processDefinitionId;
    private String processDefinitionKey;
    private String processDefinitionName;
    private int processDefinitionVersion;
    private String businessKey;
    private String deploymentId;
    private Date startTime;
    private String startUserId;
    private String name;
    private String description;
    private String tenantId;
    private boolean suspended;
    private boolean ended;
}
