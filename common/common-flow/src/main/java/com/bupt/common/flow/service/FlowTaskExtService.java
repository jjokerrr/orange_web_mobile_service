package com.bupt.common.flow.service;

import com.alibaba.fastjson.JSONObject;
import com.bupt.common.flow.model.*;
import com.bupt.common.flow.object.FlowElementExtProperty;
import com.bupt.common.flow.vo.FlowUserInfoVo;
import com.bupt.common.core.base.service.IBaseService;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.ExtensionElement;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.UserTask;
import org.flowable.task.api.TaskInfo;

import java.util.List;
import java.util.Map;

/**
 * 流程任务扩展数据操作服务接口。
 *
 * @author zzh
 * @date 2023-08-10
 */
public interface FlowTaskExtService extends IBaseService<FlowTaskExt, String> {

    /**
     * 批量插入流程任务扩展信息列表。
     *
     * @param flowTaskExtList 流程任务扩展信息列表。
     */
    void saveBatch(List<FlowTaskExt> flowTaskExtList);

    /**
     * 查询指定的流程任务扩展对象。
     *
     * @param processDefinitionId 流程引擎的定义Id。
     * @param taskId              流程引擎的任务Id。
     * @return 查询结果。
     */
    FlowTaskExt getByProcessDefinitionIdAndTaskId(String processDefinitionId, String taskId);

    /**
     * 查询指定的流程定义的任务扩展对象。
     *
     * @param processDefinitionId 流程引擎的定义Id。
     * @return 查询结果。
     */
    List<FlowTaskExt> getByProcessDefinitionId(String processDefinitionId);

    /**
     * 获取任务扩展信息中的候选人用户信息列表。
     *
     * @param processInstanceId   流程引擎的实例Id。
     * @param flowTaskExt         任务扩展对象。
     * @param taskInfo            任务信息。
     * @param isMultiInstanceTask 是否为多实例任务。
     * @param historic            是否为历史任务。
     * @return 候选人用户信息列表。
     */
    List<FlowUserInfoVo> getCandidateUserInfoList(
           String processInstanceId,
           FlowTaskExt flowTaskExt,
           TaskInfo taskInfo,
           boolean isMultiInstanceTask,
           boolean historic);

    /**
     * 通过UserTask对象中的扩展节点信息，构建FLowTaskExt对象。
     *
     * @param userTask 流程图中定义的用户任务对象。
     * @return 构建后的流程任务扩展信息对象。
     */
    FlowTaskExt buildTaskExtByUserTask(UserTask userTask);

    /**
     * 获取指定流程图中所有UserTask对象的扩展节点信息，构建FLowTaskExt对象列表。
     *
     * @param bpmnModel 流程图模型对象。
     * @return 当前流程图中所有用户流程任务的扩展信息对象列表。
     */
    List<FlowTaskExt> buildTaskExtList(BpmnModel bpmnModel);

    /**
     * 根据流程定义中用户任务的扩展节点数据，构建出前端所需的操作列表数据对象。
     * @param extensionMap 用户任务的扩展节点。
     * @return 前端所需的操作列表数据对象。
     */
    List<JSONObject> buildOperationListExtensionElement(Map<String, List<ExtensionElement>> extensionMap);

    /**
     * 根据流程定义中用户任务的扩展节点数据，构建出前端所需的变量列表数据对象。
     * @param extensionMap 用户任务的扩展节点。
     * @return 前端所需的变量列表数据对象。
     */
    List<JSONObject> buildVariableListExtensionElement(Map<String, List<ExtensionElement>> extensionMap);

    /**
     * 读取流程定义中，流程元素的扩展属性数据。
     *
     * @param element 流程图中定义的流程元素。
     * @return 流程元素的扩展属性数据。
     */
    FlowElementExtProperty buildFlowElementExt(FlowElement element);

    /**
     * 读取流程定义中，流程元素的扩展属性数据。
     *
     * @param element 流程图中定义的流程元素。
     * @return 流程元素的扩展属性数据，并转换为JSON对象。
     */
    JSONObject buildFlowElementExtToJson(FlowElement element);
}
