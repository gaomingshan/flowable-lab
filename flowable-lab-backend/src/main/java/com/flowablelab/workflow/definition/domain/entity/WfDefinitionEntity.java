package com.flowablelab.workflow.definition.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("wf_definition")
public class WfDefinitionEntity {

    @TableId(type = IdType.INPUT)
    private String id;

    private String definitionKey;
    private String definitionName;
    private String categoryCode;
    private String description;
    private String status;
    private Integer latestDraftVersion;
    private Integer latestReleasedVersion;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String latestVersionId;
}
