package com.flowable.lab.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseInstanceDTO {
    private String id;
    private String caseDefinitionId;
    private String caseDefinitionKey;
    private String caseDefinitionName;
    private String name;
    private String businessKey;
    private String state;
    private Date startTime;
    private String startUserId;
    private String tenantId;
}
