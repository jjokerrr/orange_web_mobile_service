package com.bupt.common.flow.object;

import lombok.Data;

/**
 * 表示多实例任务的指派人信息。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
public class FlowTaskMultiSignAssign {

    /**
     * 指派人类型。参考常量类 UserFilterGroup。
     */
    private String assigneeType;
    /**
     * 逗号分隔的指派人列表。
     */
    private String assigneeList;
}
