package com.bupt.common.mobile.object;

import lombok.Data;

/**
 * 移动端入口扩展数据。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
public class MobileEntryExtraData {

    /**
     * 路由名称。
     */
    private String formRouterName;

    /**
     * 在线表单。
     */
    private Long onlineFormId;

    /**
     * 报表页面。
     */
    private Long reportPageId;

    /**
     * 流程。
     */
    private Long onlineFlowEntryId;
}
