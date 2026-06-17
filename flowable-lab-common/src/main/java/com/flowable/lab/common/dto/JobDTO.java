package com.flowable.lab.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobDTO {
    private String id;
    private Date dueDate;
    private Date createTime;
    private String processInstanceId;
    private String processDefinitionId;
    private String executionId;
    private int retries;
    private String exceptionMessage;
    private String tenantId;
    private String jobType;
}
