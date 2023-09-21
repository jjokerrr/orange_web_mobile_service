package com.bupt.common.flow.service;

import com.bupt.common.core.base.service.IBaseService;
import com.bupt.common.flow.model.FlowMultiInstanceTrans;

/**
 * 会签任务操作流水数据操作服务接口。
 *
 * @author zzh
 * @date 2023-08-10
 */
public interface FlowMultiInstanceTransService extends IBaseService<FlowMultiInstanceTrans, Long> {

    /**
     * 保存新增对象。
     *
     * @param flowMultiInstanceTrans 新增对象。
     * @return 返回新增对象。
     */
    FlowMultiInstanceTrans saveNew(FlowMultiInstanceTrans flowMultiInstanceTrans);

    /**
     * 根据流程执行Id获取对象。
     *
     * @param executionId 流程执行Id。
     * @param taskId      执行任务Id。
     * @return 数据对象。
     */
    FlowMultiInstanceTrans getByExecutionId(String executionId, String taskId);

    /**
     * 根据多实例的统一执行Id，获取assgineeList字段不为空的数据。
     *
     * @param multiInstanceExecId 多实例统一执行Id。
     * @return 数据对象。
     */
    FlowMultiInstanceTrans getWithAssigneeListByMultiInstanceExecId(String multiInstanceExecId);
}
