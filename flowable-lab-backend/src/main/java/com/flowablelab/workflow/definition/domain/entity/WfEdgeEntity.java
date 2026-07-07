package com.flowablelab.workflow.definition.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("wf_edge")
public class WfEdgeEntity {

    @TableId(type = IdType.INPUT)
    private String id;
    private String versionId;
    private String edgeKey;
    private String sourceNodeKey;
    private String targetNodeKey;
    private String edgeType;
    private String conditionExpression;
    private Boolean isDefault;
    private Integer sortOrder;
}
