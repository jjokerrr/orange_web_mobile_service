package com.bupt.common.flow.service;

import com.alibaba.fastjson.JSONObject;
import com.bupt.common.core.object.CallResult;
import com.bupt.common.core.object.MyPageData;
import com.bupt.common.core.object.MyPageParam;
import com.bupt.common.core.object.Tuple2;
import com.bupt.common.flow.object.AnalyzedNode;
import com.bupt.common.flow.model.FlowTaskComment;
import com.bupt.common.flow.model.FlowTaskExt;
import com.bupt.common.flow.vo.FlowTaskVo;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FieldExtension;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskInfo;
import org.flowable.task.api.history.HistoricTaskInstance;

import javax.xml.stream.XMLStreamException;
import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 流程引擎API的接口封装服务。
 *
 * @author zzh
 * @date 2023-08-10
 */
public interface FlowApiService {

    /**
     * 启动流程实例。
     *
     * @param processDefinitionId 流程定义Id。
     * @param dataId              业务主键Id。
     * @return 新启动的流程实例。
     */
    ProcessInstance start(String processDefinitionId, Object dataId);

    /**
     * 完成第一个用户任务。
     *
     * @param processInstanceId 流程实例Id。
     * @param flowTaskComment   审批对象。
     * @param taskVariableData  流程任务的变量数据。
     * @return 新完成的任务对象。
     */
    Task takeFirstTask(String processInstanceId, FlowTaskComment flowTaskComment, JSONObject taskVariableData);

    /**
     * 启动流程实例，如果当前登录用户为第一个用户任务的指派者，或者Assginee为流程启动人变量时，
     * 则自动完成第一个用户任务。
     *
     * @param processDefinitionId 流程定义Id。
     * @param dataId              当前流程主表的主键数据。
     * @param flowTaskComment     审批对象。
     * @param taskVariableData    流程任务的变量数据。
     * @return 新启动的流程实例。
     */
    ProcessInstance startAndTakeFirst(
            String processDefinitionId, Object dataId, FlowTaskComment flowTaskComment, JSONObject taskVariableData);

    /**
     * 多实例加签减签。
     *
     * @param startTaskInstance       会签对象的发起任务实例。
     * @param multiInstanceActiveTask 正在执行的多实例任务对象。
     * @param newAssignees            新指派人，多个指派人之间逗号分隔。
     * @param isAdd                   是否为加签。
     */
    void submitConsign(HistoricTaskInstance startTaskInstance, Task multiInstanceActiveTask, String newAssignees, boolean isAdd);

    /**
     * 完成任务，同时提交审批数据。
     *
     * @param task             工作流任务对象。
     * @param flowTaskComment  审批对象。
     * @param taskVariableData 流程任务的变量数据。
     */
    void completeTask(Task task, FlowTaskComment flowTaskComment, JSONObject taskVariableData);

    /**
     * 判断当前登录用户是否为流程实例中的用户任务的指派人。或是候选人之一，如果是候选人则拾取该任务并成为指派人。
     * 如果都不是，就会返回具体的错误信息。
     *
     * @param task 流程实例中的用户任务。
     * @return 调用结果。
     */
    CallResult verifyAssigneeOrCandidateAndClaim(Task task);

    /**
     * 初始化并返回流程实例的变量Map。
     *
     * @param processDefinitionId 流程定义Id。
     * @return 初始化后的流程实例变量Map。
     */
    Map<String, Object> initAndGetProcessInstanceVariables(String processDefinitionId);

    /**
     * 判断当前登录用户是否为流程实例中的用户任务的指派人。或是候选人之一。
     *
     * @param task 流程实例中的用户任务。
     * @return 是返回true，否则false。
     */
    boolean isAssigneeOrCandidate(TaskInfo task);

    /**
     * 获取指定流程定义的全部流程节点。
     *
     * @param processDefinitionId 流程定义Id。
     * @return 当前流程定义的全部节点集合。
     */
    Collection<FlowElement> getProcessAllElements(String processDefinitionId);

    /**
     * 判断当前登录用户是否为流程实例的发起人。
     *
     * @param processInstanceId 流程实例Id。
     * @return 是返回true，否则false。
     */
    boolean isProcessInstanceStarter(String processInstanceId);

    /**
     * 为流程实例设置BusinessKey。
     *
     * @param processInstanceId 流程实例Id。
     * @param dataId            通常为主表的主键Id。
     */
    void setBusinessKeyForProcessInstance(String processInstanceId, Object dataId);

    /**
     * 判断指定的流程实例Id是否存在。
     *
     * @param processInstanceId 流程实例Id。
     * @return 存在返回true，否则false。
     */
    boolean existActiveProcessInstance(String processInstanceId);

    /**
     * 获取指定的流程实例对象。
     *
     * @param processInstanceId 流程实例Id。
     * @return 流程实例对象。
     */
    ProcessInstance getProcessInstance(String processInstanceId);

    /**
     * 获取流程实例的列表。
     *
     * @param processInstanceIdSet 流程实例Id集合。
     * @return 流程实例列表。
     */
    List<ProcessInstance> getProcessInstanceList(Set<String> processInstanceIdSet);

    /**
     * 根据流程定义Id查询流程定义对象。
     *
     * @param processDefinitionId 流程定义Id。
     * @return 流程定义对象。
     */
    ProcessDefinition getProcessDefinitionById(String processDefinitionId);

    /**
     * 根据流程部署Id查询流程定义对象。
     *
     * @param deployId 流程部署Id。
     * @return 流程定义对象。
     */
    ProcessDefinition getProcessDefinitionByDeployId(String deployId);

    /**
     * 获取流程定义的列表。
     *
     * @param processDefinitionIdSet 流程定义Id集合。
     * @return 流程定义列表。
     */
    List<ProcessDefinition> getProcessDefinitionList(Set<String> processDefinitionIdSet);

    /**
     * 挂起流程定义对象。
     *
     * @param processDefinitionId 流程定义Id。
     */
    void suspendProcessDefinition(String processDefinitionId);

    /**
     * 激活流程定义对象。
     *
     * @param processDefinitionId 流程定义Id。
     */
    void activateProcessDefinition(String processDefinitionId);

    /**
     * 获取指定流程定义的BpmnModel。
     *
     * @param processDefinitionId 流程定义Id。
     * @return 关联的BpmnModel。
     */
    BpmnModel getBpmnModelByDefinitionId(String processDefinitionId);

    /**
     * 判断任务是否为多实例任务。
     *
     * @param processDefinitionId 流程定义Id。
     * @param taskKey             流程任务标识。
     * @return true为多实例，否则false。
     */
    boolean isMultiInstanceTask(String processDefinitionId, String taskKey);

    /**
     * 设置流程实例的变量集合。
     *
     * @param processInstanceId 流程实例Id。
     * @param variableMap       变量名。
     */
    void setProcessInstanceVariables(String processInstanceId, Map<String, Object> variableMap);

    /**
     * 获取流程实例的变量。
     *
     * @param processInstanceId 流程实例Id。
     * @param variableName      变量名。
     * @return 变量值。
     */
    Object getProcessInstanceVariable(String processInstanceId, String variableName);

    /**
     * 获取指定流程实例和任务Id的当前活动任务。
     *
     * @param processInstanceId 流程实例Id。
     * @param taskId            流程任务Id。
     * @return 当前流程实例的活动任务。
     */
    Task getProcessInstanceActiveTask(String processInstanceId, String taskId);

    /**
     * 获取指定流程实例的当前活动任务列表。
     *
     * @param processInstanceId 流程实例Id。
     * @return 当前流程实例的活动任务。
     */
    List<Task> getProcessInstanceActiveTaskList(String processInstanceId);

    /**
     * 根据任务Id，获取当前运行时任务。
     *
     * @param taskId 任务Id。
     * @return 运行时任务对象。
     */
    Task getTaskById(String taskId);

    /**
     * 获取用户的任务列表。这其中包括当前用户作为指派人和候选人。
     *
     * @param username       指派人。
     * @param definitionKey  流程定义的标识。
     * @param definitionName 流程定义名。
     * @param taskName       任务名称。
     * @param pageParam      分页对象。
     * @return 用户的任务列表。
     */
    MyPageData<Task> getTaskListByUserName(
            String username, String definitionKey, String definitionName, String taskName, MyPageParam pageParam);

    /**
     * 获取用户的任务数量。这其中包括当前用户作为指派人和候选人。
     *
     * @param username 指派人。
     * @return 用户的任务数量。
     */
    long getTaskCountByUserName(String username);

    /**
     * 获取流程实例Id集合的运行时任务列表。
     *
     * @param processInstanceIdSet 流程实例Id集合。
     * @return 运行时任务列表。
     */
    List<Task> getTaskListByProcessInstanceIds(List<String> processInstanceIdSet);

    /**
     * 将流程任务列表数据，转换为前端可以显示的流程对象。
     *
     * @param taskList 流程引擎中的任务列表。
     * @return 前端可以显示的流程任务列表。
     */
    List<FlowTaskVo> convertToFlowTaskList(List<Task> taskList);

    /**
     * 添加流程实例结束的监听器。
     *
     * @param bpmnModel     流程模型。
     * @param listenerClazz 流程监听器的Class对象。
     */
    void addProcessInstanceEndListener(BpmnModel bpmnModel, Class<? extends ExecutionListener> listenerClazz);

    /**
     * 添加流程任务的执行监听器。
     *
     * @param flowElement     指定任务节点。
     * @param listenerClazz   执行监听器。
     * @param event           事件。
     * @param fieldExtensions 执行监听器的扩展变量列表。
     */
    void addExecutionListener(
            FlowElement flowElement,
            Class<? extends ExecutionListener> listenerClazz,
            String event,
            List<FieldExtension> fieldExtensions);

    /**
     * 添加流程任务创建的任务监听器。
     *
     * @param userTask      用户任务。
     * @param listenerClazz 任务监听器。
     */
    void addTaskCreateListener(UserTask userTask, Class<? extends TaskListener> listenerClazz);

    /**
     * 获取流程实例的历史流程实例。
     *
     * @param processInstanceId 流程实例Id。
     * @return 历史流程实例。
     */
    HistoricProcessInstance getHistoricProcessInstance(String processInstanceId);

    /**
     * 获取流程实例的历史流程实例列表。
     *
     * @param processInstanceIdSet 流程实例Id集合。
     * @return 历史流程实例列表。
     */
    List<HistoricProcessInstance> getHistoricProcessInstanceList(Set<String> processInstanceIdSet);

    /**
     * 查询历史流程实例的列表。
     *
     * @param processDefinitionKey  流程标识名。
     * @param processDefinitionName 流程名。
     * @param startUser             流程发起用户。
     * @param beginDate             流程发起开始时间。
     * @param endDate               流程发起结束时间。
     * @param pageParam             分页对象。
     * @param finishedOnly          仅仅返回已经结束的流程。
     * @return 分页后的查询列表对象。
     * @throws ParseException 日期参数解析失败。
     */
    MyPageData<HistoricProcessInstance> getHistoricProcessInstanceList(
            String processDefinitionKey,
            String processDefinitionName,
            String startUser,
            String beginDate,
            String endDate,
            MyPageParam pageParam,
            boolean finishedOnly) throws ParseException;

    /**
     * 获取流程实例的已完成历史任务列表。
     *
     * @param processInstanceId 流程实例Id。
     * @return 流程实例已完成的历史任务列表。
     */
    List<HistoricActivityInstance> getHistoricActivityInstanceList(String processInstanceId);

    /**
     * 获取流程实例的已完成历史任务列表，同时按照每个活动实例的开始时间升序排序。
     *
     * @param processInstanceId 流程实例Id。
     * @return 流程实例已完成的历史任务列表。
     */
    List<HistoricActivityInstance> getHistoricActivityInstanceListOrderByStartTime(String processInstanceId);

    /**
     * 获取当前用户的历史已办理任务列表。
     *
     * @param processDefinitionName 流程名。
     * @param beginDate             流程发起开始时间。
     * @param endDate               流程发起结束时间。
     * @param pageParam             分页对象。
     * @return 分页后的查询列表对象。
     * @throws ParseException 日期参数解析失败。
     */
    MyPageData<HistoricTaskInstance> getHistoricTaskInstanceFinishedList(
            String processDefinitionName,
            String beginDate,
            String endDate,
            MyPageParam pageParam) throws ParseException;

    /**
     * 获取指定的历史任务实例。
     *
     * @param processInstanceId 流程实例Id。
     * @param taskId            任务Id。
     * @return 历史任务实例。
     */
    HistoricTaskInstance getHistoricTaskInstance(String processInstanceId, String taskId);

    /**
     * 获取流程实例的待完成任务列表。
     *
     * @param processInstanceId 流程实例Id。
     * @return 流程实例待完成的任务列表。
     */
    List<HistoricActivityInstance> getHistoricUnfinishedInstanceList(String processInstanceId);

    /**
     * 终止流程实例，将任务从当前节点直接流转到主流程的结束事件。
     *
     * @param processInstanceId 流程实例Id。
     * @param stopReason        停止原因。
     * @param forCancel         是否由取消工单触发。
     * @return 执行结果。
     */
    CallResult stopProcessInstance(String processInstanceId, String stopReason, boolean forCancel);

    /**
     * 终止流程实例，将任务从当前节点直接流转到主流程的结束事件。
     *
     * @param processInstanceId 流程实例Id。
     * @param stopReason        停止原因。
     * @param status            流程状态。
     * @return 执行结果。
     */
    CallResult stopProcessInstance(String processInstanceId, String stopReason, int status);

    /**
     * 删除流程实例。
     *
     * @param processInstanceId 流程实例Id。
     */
    void deleteProcessInstance(String processInstanceId);

    /**
     * 获取任务的指定本地变量。
     *
     * @param taskId       任务Id。
     * @param variableName 变量名。
     * @return 变量值。
     */
    Object getTaskVariable(String taskId, String variableName);

    /**
     * 安全的获取任务变量，并返回字符型的变量值。
     *
     * @param taskId       任务Id。
     * @param variableName 变量名。
     * @return 返回变量值的字符串形式，如果变量不存在不会抛异常，返回null。
     */
    String getTaskVariableStringWithSafe(String taskId, String variableName);

    /**
     * 获取任务执行时的指定本地变量。
     *
     * @param executionId  任务执行时Id。
     * @param variableName 变量名。
     * @return 变量值。
     */
    Object getExecutionVariable(String executionId, String variableName);

    /**
     * 安全的获取任务执行时变量，并返回字符型的变量值。
     *
     * @param executionId  任务执行时Id。
     * @param variableName 变量名。
     * @return 返回变量值的字符串形式，如果变量不存在不会抛异常，返回null。
     */
    String getExecutionVariableStringWithSafe(String executionId, String variableName);

    /**
     * 获取历史流程变量。
     *
     * @param processInstanceId 流程实例Id。
     * @param variableName      变量名。
     * @return 获取历史流程变量。
     */
    Object getHistoricProcessInstanceVariable(String processInstanceId, String variableName);

    /**
     * 将xml格式的流程模型字符串，转换为标准的流程模型。
     *
     * @param bpmnXml xml格式的流程模型字符串。
     * @return 转换后的标准的流程模型。
     * @throws XMLStreamException XML流处理异常
     */
    BpmnModel convertToBpmnModel(String bpmnXml) throws XMLStreamException;

    /**
     * 回退到指定用户任务节点。如果没有指定，则回退到上一个任务。
     *
     * @param task             当前活动任务。
     * @param targetKey        指定回退到的任务标识。如果为null，则回退到上一个任务。
     * @param backType         回退类型。
     * @param reason           驳回或者撤销的原因。
     * @param taskVariableData 流程任务变量。
     * @return 回退结果。
     */
    CallResult backToRuntimeTask(
            Task task, String targetKey, Integer backType, String reason, JSONObject taskVariableData);

    /**
     * 回退到指定用户任务节点。同时将回退任务的候选人改为指定人。
     *
     * @param task             当前活动任务。
     * @param targetKey        指定回退到的任务标识。如果为null，则回退到上一个任务。
     * @param backType         回退类型。
     * @param reason           驳回或者撤销的原因。
     * @param delegateAssignee 转办人，多人之间逗号分割。
     * @return 回退结果。
     */
    CallResult backToRuntimeTaskAndTransfer(
            Task task, String targetKey, Integer backType, String reason, String delegateAssignee);

    /**
     * 根据流程任务，获取可以回退的用户任务列表。
     *
     * @param task 流程任务。
     * @return 可以回退的用户任务列表。
     */
    List<UserTask> getRejectCandidateUserTaskList(Task task);

    /**
     * 转办任务给他人。
     *
     * @param task            流程任务。
     * @param flowTaskComment 审批对象。
     */
    void transferTo(Task task, FlowTaskComment flowTaskComment);

    /**
     * 获取当前任务在流程图中配置候选用户组数据。
     *
     * @param flowTaskExt 流程任务扩展对象。
     * @param taskId      运行时任务Id。
     * @return 候选用户组数据。
     */
    List<String> getCandidateUsernames(FlowTaskExt flowTaskExt, String taskId);

    /**
     * 获取当前任务在流程图中配置到的部门岗位Id集合和岗位Id集合。
     *
     * @param flowTaskExt       流程任务扩展对象。
     * @param processInstanceId 流程实例Id。
     * @param historic          是否为历史任务。
     * @return first为部门岗位Id集合，second是岗位Id集合。
     */
    Tuple2<Set<String>, Set<String>> getDeptPostIdAndPostIds(
            FlowTaskExt flowTaskExt, String processInstanceId, boolean historic);

    /**
     * 分析指定流程部署Id的流程图。
     *
     * @param processDefinitionId 流程定义Id。
     * @return 分析结果根对象。
     */
    AnalyzedNode analyzeBpmnRoads(String processDefinitionId);

    /**
     * 自由跳转。
     *
     * @param task             当前的待办任务。
     * @param targetKey        目标任务的Key定义。
     * @param comment          跳转说明。
     * @param delegateAssignee 指派人。
     * @return 跳转结果，如果失败，会返回详细的错误信息。
     */
    CallResult freeJumpTo(Task task, String targetKey, String comment, String delegateAssignee);

    /**
     * 流程干预。
     *
     * @param task             当前的待办任务。
     * @param targetKey        目标任务的Key定义。
     * @param comment          干预说明。
     * @param delegateAssignee 指派人。
     * @return 干预结果，如果失败，会返回详细的错误信息。
     */
    CallResult interveneTo(Task task, String targetKey, String comment, String delegateAssignee);

    /**
     * 获取指定任务的运行时子任务Id列表。
     *
     * @param processDefinitionId 流程定义Id。
     * @param taskKey             流程定义标识。
     * @param allActivitiIds      所有活动任务的定义Id列表
     * @return allActivitiIds中属于taskKey的子任务的Id列表。
     */
    List<String> getChildActivitiIdList(String processDefinitionId, String taskKey, List<String> allActivitiIds);

    /**
     * 获取流程图中所有用户任务的映射。
     *
     * @param processDefinitionId 流程定义Id。
     * @return 流程图中所有用户任务的映射。
     */
    Map<String, UserTask> getAllUserTaskMap(String processDefinitionId);
}
