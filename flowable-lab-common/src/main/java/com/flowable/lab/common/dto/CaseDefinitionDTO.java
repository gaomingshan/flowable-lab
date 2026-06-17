package com.flowable.lab.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseDefinitionDTO {
    private String id;
    private String key;
    private String name;
    private int version;
    private String description;
    private String deploymentId;
    private String tenantId;
}
