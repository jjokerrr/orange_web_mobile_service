package com.bupt.common.flow.util;

import cn.hutool.core.lang.Assert;
import com.bupt.common.flow.base.service.BaseFlowOnlineService;
import com.bupt.common.flow.model.FlowWorkOrder;
import lombok.extern.slf4j.Slf4j;

/**
 * 面向在线表单工作流的业务数据扩展帮助实现类。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Slf4j
public class BaseOnlineBusinessDataExtHelper {

    private BaseFlowOnlineService onlineBusinessService;

    /**
     * 设置在线表单的业务处理服务。
     *
     * @param onlineBusinessService 在线表单业务处理服务实现类。
     */
    public void setOnlineBusinessService(BaseFlowOnlineService onlineBusinessService) {
        this.onlineBusinessService = onlineBusinessService;
    }

    /**
     * 更新在线表单主表数据的流程状态字段值。
     *
     * @param workOrder 工单对象。
     */
    public void updateFlowStatus(FlowWorkOrder workOrder) {
        Assert.notNull(workOrder.getOnlineTableId());
        if (this.onlineBusinessService != null && workOrder.getBusinessKey() != null) {
            onlineBusinessService.updateFlowStatus(workOrder);
        }
    }

    /**
     * 根据工单对象级联删除业务数据。
     *
     * @param workOrder 工单对象。
     */
    public void deleteBusinessData(FlowWorkOrder workOrder) {
        Assert.notNull(workOrder.getOnlineTableId());
        if (this.onlineBusinessService != null && workOrder.getBusinessKey() != null) {
            onlineBusinessService.deleteBusinessData(workOrder);
        }
    }
}
