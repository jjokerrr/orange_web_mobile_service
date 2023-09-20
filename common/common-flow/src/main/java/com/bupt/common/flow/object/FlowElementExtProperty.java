package com.bupt.common.flow.object;

import lombok.Data;

/**
 * 流程任务的扩展属性。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
public class FlowElementExtProperty {

    /**
     * 最近的审批状态，该值目前仅仅用于流程线元素，即SequenceElement。
     */
    private Integer latestApprovalStatus;
}
