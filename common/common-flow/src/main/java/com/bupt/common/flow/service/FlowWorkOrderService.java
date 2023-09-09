package com.bupt.common.flow.service;

import com.bupt.common.core.base.service.IBaseService;
import com.bupt.common.core.object.CallResult;
import com.bupt.common.flow.model.FlowWorkOrder;
import com.bupt.common.flow.model.FlowWorkOrderExt;
import com.bupt.common.flow.vo.FlowWorkOrderVo;
import org.flowable.engine.runtime.ProcessInstance;

import java.util.*;

/**
 * 工作流工单表数据操作服务接口。
 *
 * @author zzh
 * @date 2023-08-10
 */
public interface FlowWorkOrderService extends IBaseService<FlowWorkOrder, Long> {

    /**
     * 保存新增对象。
     *
     * @param instance      流程实例对象。
     * @param dataId        流程实例的BusinessKey。
     * @param onlineTableId 在线数据表的主键Id。
     * @param tableName     面向静态表单所使用的表名。
     * @return 新增的工作流工单对象。
     */
    FlowWorkOrder saveNew(ProcessInstance instance, Object dataId, Long onlineTableId, String tableName);

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
    FlowWorkOrder saveNewWithDraft(
            ProcessInstance instance, Long onlineTableId, String tableName, String masterData, String slaveData);

    /**
     * 更新流程工单的草稿数据。
     *
     * @param workOrderId 工单Id。
     * @param masterData  主表数据。
     * @param slaveData   从表数据。
     */
    void updateDraft(Long workOrderId, String masterData, String slaveData);

    /**
     * 删除指定数据。
     *
     * @param workOrderId 主键Id。
     * @return 成功返回true，否则false。
     */
    boolean remove(Long workOrderId);

    /**
     * 删除指定流程实例Id的关联工单。
     *
     * @param processInstanceId 流程实例Id。
     */
    void removeByProcessInstanceId(String processInstanceId);

    /**
     * 获取工作流工单单表查询结果。
     *
     * @param filter  过滤对象。
     * @param orderBy 排序参数。
     * @return 查询结果集。
     */
    List<FlowWorkOrder> getFlowWorkOrderList(FlowWorkOrder filter, String orderBy);

    /**
     * 获取工作流工单列表及其关联字典数据。
     *
     * @param filter  过滤对象。
     * @param orderBy 排序参数。
     * @return 查询结果集。
     */
    List<FlowWorkOrder> getFlowWorkOrderListWithRelation(FlowWorkOrder filter, String orderBy);

    /**
     * 根据流程实例Id，查询关联的工单对象。
     *
     * @param processInstanceId 流程实例Id。
     * @return 工作流工单对象。
     */
    FlowWorkOrder getFlowWorkOrderByProcessInstanceId(String processInstanceId);

    /**
     * 根据业务主键，查询是否存在指定的工单。
     *
     * @param tableName   静态表单工作流使用的数据表。
     * @param businessKey 业务数据主键Id。
     * @param unfinished  是否为没有结束工单。
     * @return 存在返回true，否则false。
     */
    boolean existByBusinessKey(String tableName, Object businessKey, boolean unfinished);

    /**
     * 根据流程实例Id，更新流程状态。
     *
     * @param processInstanceId 流程实例Id。
     * @param flowStatus        新的流程状态值，如果该值为null，不执行任何更新。
     */
    void updateFlowStatusByProcessInstanceId(String processInstanceId, Integer flowStatus);

    /**
     * 根据流程实例Id，更新流程最后审批状态。
     *
     * @param processInstanceId 流程实例Id。
     * @param approvalStatus    新的流程最后审批状态，如果该值为null，不执行任何更新。
     */
    void updateLatestApprovalStatusByProcessInstanceId(String processInstanceId, Integer approvalStatus);

    /**
     * 是否有查看该工单的数据权限。
     *
     * @param processInstanceId 流程实例Id。
     * @return 存在返回true，否则false。
     */
    boolean hasDataPermOnFlowWorkOrder(String processInstanceId);

    /**
     * 根据工单列表中的submitUserName，找到映射的userShowName，并会写到Vo中指定字段。
     * 同时这也是一个如何通过插件方法，将loginName映射到showName的示例，
     *
     * @param dataList 工单Vo对象列表。
     */
    void fillUserShowNameByLoginName(List<FlowWorkOrderVo> dataList);

    /**
     * 根据工单Id获取工单扩展对象数据。
     *
     * @param workOrderId 工单Id。
     * @return 工单扩展对象。
     */
    FlowWorkOrderExt getFlowWorkOrderExtByWorkOrderId(Long workOrderId);

    /**
     * 根据工单Id集合获取工单扩展对象数据列表。
     *
     * @param workOrderIds 工单Id集合。
     * @return 工单扩展对象列表。
     */
    List<FlowWorkOrderExt> getFlowWorkOrderExtByWorkOrderIds(Set<Long> workOrderIds);

    /**
     * 移除草稿工单，同时停止已经启动的流程实例。
     *
     * @param flowWorkOrder 工单对象。
     * @return 停止流程实例的结果。
     */
    CallResult removeDraft(FlowWorkOrder flowWorkOrder);
}
