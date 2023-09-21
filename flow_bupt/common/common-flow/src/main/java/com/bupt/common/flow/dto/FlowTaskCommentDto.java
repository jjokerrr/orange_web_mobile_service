package com.bupt.common.flow.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 流程任务的批注。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
public class FlowTaskCommentDto {

    /**
     * 流程任务触发按钮类型，内置值可参考FlowTaskButton。
     */
    @NotNull(message = "数据验证失败，任务的审批类型不能为空！")
    private String approvalType;

    /**
     * 流程任务的批注内容。
     */
    @NotBlank(message = "数据验证失败，任务审批内容不能为空！")
    private String taskComment;

    /**
     * 委托指定人，比如加签、转办等。
     */
    private String delegateAssignee;
}
