package com.bupt.common.flow.service;

import com.bupt.common.core.base.service.IBaseService;
import com.bupt.common.flow.model.FlowVariableDisplay;
import com.bupt.common.flow.model.FlowWorkOrder;
import org.flowable.engine.runtime.ProcessInstance;

import java.util.List;
import java.util.Set;

/**
 * 工作流工单表数据操作服务接口。
 *
 * @author zzh
 * @date 2023-08-10
 */
public interface FlowVariableDisplayService{

    /**
     * 保存新增对象。
     *
     * @param instance      流程实例对象。
     * @param dataId        流程实例的BusinessKey。
     * @param onlineTableId 在线数据表的主键Id。
     * @param tableName     面向静态表单所使用的表名。
     * @return 新增的工作流工单对象。
     */
    FlowVariableDisplay saveNew(ProcessInstance instance, Object dataId, Long onlineTableId, String tableName);

    /**
     * 保存工单草稿。
     *
     * @param instance      流程实例对象。
     * @param onlineTableId 在线表单的主表Id。
     * @param tableName     静态表单的主表表名。
     * @param masterData    主表数据。
     * @param slaveData     从表数据。
     * @return 工单对象。
     */
    FlowVariableDisplay saveNewWithDraft(
            ProcessInstance instance, Long onlineTableId, String tableName, String masterData, String slaveData);

    boolean remove(List<String> workOrderId);

    boolean removeByEntry(String entryId);

    boolean removeByTaskKey(String taskKey);

    boolean updateByEntry(String entryId, List<FlowVariableDisplay> flowVaribleDisplayList);

    List<FlowVariableDisplay> select(String taskKey);

    List<FlowVariableDisplay> selectByEntry(String entryId);
}
