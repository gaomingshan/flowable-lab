package com.flowable.lab.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeploymentDTO {
    private String id;
    private String name;
    private Date deploymentTime;
    private String tenantId;
}
