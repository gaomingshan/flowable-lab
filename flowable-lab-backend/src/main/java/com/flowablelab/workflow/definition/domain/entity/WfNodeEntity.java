package com.flowablelab.workflow.definition.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("wf_node")
public class WfNodeEntity {

    @TableId(type = IdType.INPUT)
    private String id;
    private String versionId;
    private String nodeKey;
    private String nodeName;
    private String nodeType;
    private Integer incomingCount;
    private Integer outgoingCount;
    private Integer sortOrder;
}
