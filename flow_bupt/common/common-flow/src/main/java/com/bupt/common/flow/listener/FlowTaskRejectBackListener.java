package com.bupt.common.flow.listener;

import com.bupt.common.flow.constant.FlowConstant;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;

import java.util.Map;

/**
 * 任务驳回后重新提交到该任务的通知监听器。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Slf4j
public class FlowTaskRejectBackListener implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        Map<String, Object> variables = delegateTask.getVariables();
        if (variables.get(FlowConstant.REJECT_BACK_TO_SOURCE_DATA_VAR) != null) {
            delegateTask.setAssignee(variables.get(FlowConstant.REJECT_BACK_TO_SOURCE_DATA_VAR).toString());
        }
    }
}
