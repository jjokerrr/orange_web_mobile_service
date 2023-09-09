package com.bupt.common.flow.service;

import com.bupt.common.flow.model.*;
import com.bupt.common.core.base.service.IBaseService;

import java.util.*;

/**
 * 流程任务批注数据操作服务接口。
 *
 * @author zzh
 * @date 2023-08-10
 */
public interface FlowTaskCommentService extends IBaseService<FlowTaskComment, Long> {

    /**
     * 保存新增对象。
     *
     * @param flowTaskComment 新增对象。
     * @return 返回新增对象。
     */
    FlowTaskComment saveNew(FlowTaskComment flowTaskComment);

    /**
     * 查询指定流程实例Id下的所有审批任务的批注。
     *
     * @param processInstanceId 流程实例Id。
     * @return 查询结果集。
     */
    List<FlowTaskComment> getFlowTaskCommentList(String processInstanceId);

    /**
     * 查询与指定流程任务Id集合关联的所有审批任务的批注。
     *
     * @param taskIdSet 流程任务Id集合。
     * @return 查询结果集。
     */
    List<FlowTaskComment> getFlowTaskCommentListByTaskIds(Set<String> taskIdSet);

    /**
     * 获取指定流程实例的最后一条审批任务。
     *
     * @param processInstanceId 流程实例Id。
     * @return 查询结果。
     */
    FlowTaskComment getLatestFlowTaskComment(String processInstanceId);

    /**
     * 获取指定流程实例和任务定义标识的最后一条审批任务。
     *
     * @param processInstanceId 流程实例Id。
     * @param taskDefinitionKey 任务定义标识。
     * @return 查询结果。
     */
    FlowTaskComment getLatestFlowTaskComment(String processInstanceId, String taskDefinitionKey);

    /**
     * 获取指定流程实例的第一条审批任务。
     *
     * @param processInstanceId 流程实例Id。
     * @return 查询结果。
     */
    FlowTaskComment getFirstFlowTaskComment(String processInstanceId);

    /**
     * 获取指定任务实例和执行批次的审批数据列表。
     *
     * @param processInstanceId 流程实例。
     * @param taskId            任务Id
     * @param executionId       任务执行Id
     * @return 审批数据列表。
     */
    List<FlowTaskComment> getFlowTaskCommentListByExecutionId(
            String processInstanceId, String taskId, String executionId);

    /**
     * 根据多实例执行Id获取任务审批对象数据列表。
     *
     * @param multiInstanceExecId 多实例执行Id。
     * @return 审批数据列表。
     */
    List<FlowTaskComment> getFlowTaskCommentListByMultiInstanceExecId(String multiInstanceExecId);
}
