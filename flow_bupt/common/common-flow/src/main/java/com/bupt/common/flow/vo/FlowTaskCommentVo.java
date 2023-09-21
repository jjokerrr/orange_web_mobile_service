package com.bupt.common.flow.vo;

import lombok.Data;

import java.util.Date;

/**
 * FlowTaskCommentVO对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
public class FlowTaskCommentVo {

    /**
     * 主键Id。
     */
    private Long id;

    /**
     * 流程实例Id。
     */
    private String processInstanceId;

    /**
     * 任务Id。
     */
    private String taskId;

    /**
     * 任务标识。
     */
    private String taskKey;

    /**
     * 任务名称。
     */
    private String taskName;

    /**
     * 任务的执行Id。
     */
    private String executionId;

    /**
     * 会签任务的执行Id。
     */
    private String multiInstanceExecId;

    /**
     * 审批类型。
     */
    private String approvalType;

    /**
     * 批注内容。
     */
    private String taskComment;

    /**
     * 委托指定人，比如加签、转办等。
     */
    private String delegateAssignee;

    /**
     * 自定义数据。开发者可自行扩展，推荐使用JSON格式数据。
     */
    private String customBusinessData;

    /**
     * 审批人头像。
     */
    private String headImageUrl;

    /**
     * 创建者Id。
     */
    private Long createUserId;

    /**
     * 创建者登录名。
     */
    private String createLoginName;

    /**
     * 创建者显示名。
     */
    private String createUsername;

    /**
     * 创建时间。
     */
    private Date createTime;
}
