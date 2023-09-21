package com.bupt.common.flow.vo;

import lombok.Data;

import java.util.Date;

/**
 * 工作流通知消息Vo对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
public class FlowMessageVo {

    /**
     * 主键Id。
     */
    private Long messageId;

    /**
     * 消息类型。
     */
    private Integer messageType;

    /**
     * 消息内容。
     */
    private String messageContent;

    /**
     * 催办次数。
     */
    private Integer remindCount;

    /**
     * 工单Id。
     */
    private Long workOrderId;

    /**
     * 流程定义Id。
     */
    private String processDefinitionId;

    /**
     * 流程定义标识。
     */
    private String processDefinitionKey;

    /**
     * 流程名称。
     */
    private String processDefinitionName;

    /**
     * 流程实例Id。
     */
    private String processInstanceId;

    /**
     * 流程实例发起者。
     */
    private String processInstanceInitiator;

    /**
     * 流程任务Id。
     */
    private String taskId;

    /**
     * 流程任务定义标识。
     */
    private String taskDefinitionKey;

    /**
     * 流程任务名称。
     */
    private String taskName;

    /**
     * 创建时间。
     */
    private Date taskStartTime;

    /**
     * 业务数据快照。
     */
    private String businessDataShot;

    /**
     * 更新时间。
     */
    private Date updateTime;

    /**
     * 更新者Id。
     */
    private Long updateUserId;

    /**
     * 创建时间。
     */
    private Date createTime;

    /**
     * 创建者Id。
     */
    private Long createUserId;

    /**
     * 创建者显示名。
     */
    private String createUsername;
}
