package com.bupt.common.flow.object;

import lombok.Data;

/**
 * 流程图中的用户任务操作数据。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
public class FlowTaskOperation {

    /**
     * 操作Id。
     */
    private String id;
    /**
     * 操作的标签名。
     */
    private String label;
    /**
     * 操作类型。
     */
    private String type;
    /**
     * 显示顺序。
     */
    private Integer showOrder;
    /**
     * 在流程图中定义的多实例会签的指定人员信息。
     */
    private FlowTaskMultiSignAssign multiSignAssignee;
}
