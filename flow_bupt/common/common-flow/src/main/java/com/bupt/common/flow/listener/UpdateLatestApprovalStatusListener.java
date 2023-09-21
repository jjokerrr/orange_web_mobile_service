package com.bupt.common.flow.listener;

import cn.hutool.core.util.StrUtil;
import com.bupt.common.core.util.ApplicationContextHolder;
import com.bupt.common.flow.model.FlowWorkOrder;
import com.bupt.common.flow.service.FlowWorkOrderService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.engine.impl.el.FixedValue;

/**
 * 更新流程的最后审批状态的监听器，目前用于排他网关到任务结束节点的连线上，
 * 以便于准确的判断流程实例的最后审批状态。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Slf4j
public class UpdateLatestApprovalStatusListener implements ExecutionListener {

    private FixedValue latestApprovalStatus;

    private final transient FlowWorkOrderService flowWorkOrderService =
            ApplicationContextHolder.getBean(FlowWorkOrderService.class);

    public void setAutoStoreVariablesExp(FixedValue approvalStatus) {
        this.latestApprovalStatus = approvalStatus;
    }

    @Override
    public void notify(DelegateExecution execution) {
        if (StrUtil.isNotBlank(latestApprovalStatus.getExpressionText())) {
            FlowWorkOrder workOrder =
                    flowWorkOrderService.getFlowWorkOrderByProcessInstanceId(execution.getProcessInstanceId());
            if (workOrder == null) {
                return;
            }
            Integer approvalStatus = Integer.valueOf(latestApprovalStatus.getExpressionText());
            String processInstanceId = execution.getProcessInstanceId();
            flowWorkOrderService.updateLatestApprovalStatusByProcessInstanceId(processInstanceId, approvalStatus);
        }
    }
}
