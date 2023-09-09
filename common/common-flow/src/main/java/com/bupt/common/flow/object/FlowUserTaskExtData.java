package com.bupt.common.flow.object;

import lombok.Data;

import java.util.List;

/**
 * 流程用户任务扩展数据对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
public class FlowUserTaskExtData {

    public static final String NOTIFY_TYPE_MSG = "message";
    public static final String NOTIFY_TYPE_EMAIL = "email";
    /**
     * 拒绝后再提交，走重新审批。
     */
    public static final String REJECT_TYPE_REDO = "0";
    /**
     * 拒绝后再提交，直接回到驳回前的节点。
     */
    public static final String REJECT_TYPE_BACK_TO_SOURCE = "1";

    /**
     * 任务通知类型列表。
     */
    private List<String> flowNotifyTypeList;
    /**
     * 拒绝后再次提交的审批类型。
     */
    private String rejectType = REJECT_TYPE_REDO;
}
