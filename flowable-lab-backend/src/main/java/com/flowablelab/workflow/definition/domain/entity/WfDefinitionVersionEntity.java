package com.flowablelab.workflow.definition.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("wf_definition_version")
public class WfDefinitionVersionEntity {

    @TableId(type = IdType.INPUT)
    private String id;
    private String definitionId;
    private Integer versionNo;
    private String changeNote;
    private String status;
    private String graphJson;
    private String releaseComment;
    private String deploymentId;
    private String processDefinitionId;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime releasedAt;
}
