package com.bupt.common.flow.listener;

import com.bupt.common.core.util.ApplicationContextHolder;
import com.bupt.common.flow.constant.FlowConstant;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;

import java.util.Map;

/**
 * 流程任务通用监听器。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Slf4j
public class FlowUserTaskListener implements TaskListener {

    private final transient RuntimeService runtimeService =
            ApplicationContextHolder.getBean(RuntimeService.class);

    @Override
    public void notify(DelegateTask delegateTask) {
        Map<String, Object> variables = delegateTask.getVariables();
        if (variables.get(FlowConstant.DELEGATE_ASSIGNEE_VAR) != null) {
            delegateTask.setAssignee(variables.get(FlowConstant.DELEGATE_ASSIGNEE_VAR).toString());
            runtimeService.removeVariableLocal(delegateTask.getExecutionId(), FlowConstant.DELEGATE_ASSIGNEE_VAR);
        }
    }
}
