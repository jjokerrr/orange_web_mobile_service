package com.bupt.common.flow.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.*;
import cn.hutool.core.convert.Convert;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.bupt.common.core.annotation.MultiDatabaseWriteMethod;
import com.bupt.common.core.annotation.MyDataSourceResolver;
import com.bupt.common.core.constant.ApplicationConstant;
import com.bupt.common.core.constant.UserFilterGroup;
import com.bupt.common.core.exception.MyRuntimeException;
import com.bupt.common.core.object.*;
import com.bupt.common.core.util.MyDateUtil;
import com.bupt.common.core.util.MyCommonUtil;
import com.bupt.common.core.util.DefaultDataSourceResolver;
import com.bupt.common.flow.exception.FlowOperationException;
import com.bupt.common.flow.object.*;
import com.bupt.common.flow.constant.FlowBackType;
import com.bupt.common.flow.constant.FlowConstant;
import com.bupt.common.flow.constant.FlowApprovalType;
import com.bupt.common.flow.constant.FlowTaskStatus;
import com.bupt.common.flow.model.*;
import com.bupt.common.flow.service.*;
import com.bupt.common.flow.util.BaseFlowIdentityExtHelper;
import com.bupt.common.flow.util.FlowCustomExtFactory;
import com.bupt.common.flow.vo.FlowTaskVo;
import com.bupt.common.flow.vo.FlowUserInfoVo;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.*;
import org.flowable.bpmn.model.Process;
import org.flowable.common.engine.impl.de.odysseus.el.ExpressionFactoryImpl;
import org.flowable.common.engine.impl.de.odysseus.el.util.SimpleContext;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.common.engine.impl.javax.el.ExpressionFactory;
import org.flowable.common.engine.impl.javax.el.ValueExpression;
import org.flowable.engine.*;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.engine.history.*;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ChangeActivityStateBuilder;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceBuilder;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskInfo;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.bupt.common.flow.object.AnalyzedNode.UserTaskInfo;

@Slf4j
@MyDataSourceResolver(
        resolver = DefaultDataSourceResolver.class,
        intArg = ApplicationConstant.COMMON_FLOW_AND_ONLINE_DATASOURCE_TYPE)
@Service("flowApiService")
public class FlowApiServiceImpl implements FlowApiService {

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private FlowEntryService flowEntryService;
    @Autowired
    private FlowTaskCommentService flowTaskCommentService;
    @Autowired
    private FlowTaskExtService flowTaskExtService;
    @Autowired
    private FlowWorkOrderService flowWorkOrderService;
    @Autowired
    private FlowMessageService flowMessageService;
    @Autowired
    private FlowCustomExtFactory flowCustomExtFactory;
    @Autowired
    private FlowMultiInstanceTransService flowMultiInstanceTransService;
    @Autowired
    private FlowTransProducerService flowTransProducerService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ProcessInstance start(String processDefinitionId, Object dataId) {
        TokenData tokenData = TokenData.takeFromRequest();
        Map<String, Object> variableMap = this.initAndGetProcessInstanceVariables(processDefinitionId);
        Authentication.setAuthenticatedUserId(tokenData.getLoginName());
        String businessKey = dataId == null ? null : dataId.toString();
        ProcessInstanceBuilder builder = runtimeService.createProcessInstanceBuilder()
                .processDefinitionId(processDefinitionId).businessKey(businessKey).variables(variableMap);
        if (tokenData.getTenantId() != null) {
            builder.tenantId(tokenData.getTenantId().toString());
        } else {
            if (tokenData.getAppCode() != null) {
                builder.tenantId(tokenData.getAppCode());
            }
        }
        return builder.start();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Task takeFirstTask(String processInstanceId, FlowTaskComment flowTaskComment, JSONObject taskVariableData) {
        String loginName = TokenData.takeFromRequest().getLoginName();
        // 获取流程启动后的第一个任务。
        Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).active().singleResult();
        if (StrUtil.equalsAny(task.getAssignee(), loginName, FlowConstant.START_USER_NAME_VAR)) {
            // 按照规则，调用该方法的用户，就是第一个任务的assignee，因此默认会自动执行complete。
            flowTaskComment.fillWith(task);
            this.completeTask(task, flowTaskComment, taskVariableData);
        }
        return task;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ProcessInstance startAndTakeFirst(
            String processDefinitionId, Object dataId, FlowTaskComment flowTaskComment, JSONObject taskVariableData) {
        ProcessInstance instance = this.start(processDefinitionId, dataId);
        this.takeFirstTask(instance.getProcessInstanceId(), flowTaskComment, taskVariableData);
        return instance;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void submitConsign(
            HistoricTaskInstance startTaskInstance, Task multiInstanceActiveTask, String newAssignees, boolean isAdd) {
        JSONArray assigneeArray = JSON.parseArray(newAssignees);
        String multiInstanceExecId = this.getExecutionVariableStringWithSafe(
                multiInstanceActiveTask.getExecutionId(), FlowConstant.MULTI_SIGN_TASK_EXECUTION_ID_VAR);
        FlowMultiInstanceTrans trans =
                flowMultiInstanceTransService.getWithAssigneeListByMultiInstanceExecId(multiInstanceExecId);
        Set<String> assigneeSet = new HashSet<>(StrUtil.split(trans.getAssigneeList(), ","));
        Task runtimeTask = null;
        for (int i = 0; i < assigneeArray.size(); i++) {
            String assignee = assigneeArray.getString(i);
            if (isAdd) {
                assigneeSet.add(assignee);
            } else {
                assigneeSet.remove(assignee);
            }
            if (isAdd) {
                Map<String, Object> variables = new HashMap<>(2);
                variables.put("assignee", assigneeArray.getString(i));
                variables.put(FlowConstant.MULTI_SIGN_START_TASK_VAR, startTaskInstance.getId());
                runtimeService.addMultiInstanceExecution(
                        multiInstanceActiveTask.getTaskDefinitionKey(), multiInstanceActiveTask.getProcessInstanceId(), variables);
            } else {
                TaskQuery query = taskService.createTaskQuery().active();
                query.processInstanceId(multiInstanceActiveTask.getProcessInstanceId());
                query.taskDefinitionKey(multiInstanceActiveTask.getTaskDefinitionKey());
                query.taskAssignee(assignee);
                runtimeTask = query.singleResult();
                if (runtimeTask == null) {
                    throw new FlowOperationException("审批人 [" + assignee + "] 已经提交审批，不能执行减签操作！");
                }
                runtimeService.deleteMultiInstanceExecution(runtimeTask.getExecutionId(), false);
            }
        }
        if (!isAdd && runtimeTask != null) {
            this.doChangeTask(runtimeTask);
        }
        trans.setAssigneeList(StrUtil.join(",", assigneeSet));
        flowMultiInstanceTransService.updateById(trans);
        FlowTaskComment flowTaskComment = new FlowTaskComment();
        flowTaskComment.fillWith(startTaskInstance);
        flowTaskComment.setApprovalType(isAdd ? FlowApprovalType.MULTI_CONSIGN : FlowApprovalType.MULTI_MINUS_SIGN);
        String showName = TokenData.takeFromRequest().getLoginName();
        String comment = String.format("用户 [%s] [%s] [%s]。", isAdd ? "加签" : "减签", showName, newAssignees);
        flowTaskComment.setTaskComment(comment);
        flowTaskCommentService.saveNew(flowTaskComment);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void completeTask(Task task, FlowTaskComment flowTaskComment, JSONObject taskVariableData) {
        String value = this.getTaskVariableStringWithSafe(task.getId(), FlowConstant.REJECT_TO_SOURCE_DATA_VAR);
        if (StrUtil.isNotBlank(value) && flowTaskComment.getApprovalType().equals(FlowApprovalType.AGREE)) {
            flowTaskComment.fillWith(task);
            flowTaskCommentService.saveNew(flowTaskComment);
            Integer approvalStatus = MapUtil.getInt(taskVariableData, FlowConstant.LATEST_APPROVAL_STATUS_KEY);
            flowWorkOrderService.updateLatestApprovalStatusByProcessInstanceId(task.getProcessInstanceId(), approvalStatus);
            FlowRejectData rejectData = JSON.parseObject(value, FlowRejectData.class);
            Map<String, Object> variables = MapUtil.newHashMap();
            variables.put(FlowConstant.REJECT_BACK_TO_SOURCE_DATA_VAR, rejectData.getSourceUser());
            this.doChanageState(task.getProcessInstanceId(),
                    CollUtil.newArrayList(task.getTaskDefinitionKey()),
                    CollUtil.newArrayList(rejectData.getSourceTaskKey()), null, variables);
            flowMessageService.updateFinishedStatusByTaskId(task.getId());
            return;
        }
        JSONObject passCopyData = null;
        if (taskVariableData != null) {
            passCopyData = (JSONObject) taskVariableData.remove(FlowConstant.COPY_DATA_KEY);
        }
        if (flowTaskComment != null) {
            if (taskVariableData == null) {
                taskVariableData = new JSONObject();
            }
            // 这里处理多实例会签逻辑。
            if (flowTaskComment.getApprovalType().equals(FlowApprovalType.MULTI_SIGN)) {
                String loginName = TokenData.takeFromRequest().getLoginName();
                String assigneeList = this.getMultiInstanceAssigneeList(task, taskVariableData);
                Assert.isTrue(StrUtil.isNotBlank(assigneeList));
                taskVariableData.put(FlowConstant.MULTI_AGREE_COUNT_VAR, 0);
                taskVariableData.put(FlowConstant.MULTI_REFUSE_COUNT_VAR, 0);
                taskVariableData.put(FlowConstant.MULTI_ABSTAIN_COUNT_VAR, 0);
                taskVariableData.put(FlowConstant.MULTI_SIGN_NUM_OF_INSTANCES_VAR, 0);
                taskVariableData.put(FlowConstant.MULTI_SIGN_START_TASK_VAR, task.getId());
                String multiInstanceExecId = MyCommonUtil.generateUuid();
                taskVariableData.put(FlowConstant.MULTI_SIGN_TASK_EXECUTION_ID_VAR, multiInstanceExecId);
                String comment = String.format("用户 [%s] 会签 [%s]。", loginName, assigneeList);
                FlowMultiInstanceTrans multiInstanceTrans = new FlowMultiInstanceTrans(task);
                multiInstanceTrans.setMultiInstanceExecId(multiInstanceExecId);
                multiInstanceTrans.setAssigneeList(assigneeList);
                flowMultiInstanceTransService.saveNew(multiInstanceTrans);
                flowTaskComment.setTaskComment(comment);
            }
            // 处理转办。
            if (FlowApprovalType.TRANSFER.equals(flowTaskComment.getApprovalType())) {
                this.transferTo(task, flowTaskComment);
                return;
            }
            this.handleMultiInstanceApprovalType(
                    task.getExecutionId(), flowTaskComment.getApprovalType(), taskVariableData);
            taskVariableData.put(FlowConstant.OPERATION_TYPE_VAR, flowTaskComment.getApprovalType());
            taskVariableData.put(FlowConstant.SUBMIT_USER_VAR, TokenData.takeFromRequest().getLoginName());
            flowTaskComment.fillWith(task);
            if (this.isMultiInstanceTask(task.getProcessDefinitionId(), task.getTaskDefinitionKey())) {
                String multiInstanceExecId = getExecutionVariableStringWithSafe(
                        task.getExecutionId(), FlowConstant.MULTI_SIGN_TASK_EXECUTION_ID_VAR);
                FlowMultiInstanceTrans multiInstanceTrans = new FlowMultiInstanceTrans(task);
                multiInstanceTrans.setMultiInstanceExecId(multiInstanceExecId);
                flowMultiInstanceTransService.saveNew(multiInstanceTrans);
                flowTaskComment.setMultiInstanceExecId(multiInstanceExecId);
            }
            flowTaskCommentService.saveNew(flowTaskComment);
        }
        // 判断当前完成执行的任务，是否存在抄送设置。
        Object copyData = runtimeService.getVariable(
                task.getProcessInstanceId(), FlowConstant.COPY_DATA_MAP_PREFIX + task.getTaskDefinitionKey());
        if (copyData != null || passCopyData != null) {
            JSONObject copyDataJson = this.mergeCopyData(copyData, passCopyData);
            flowMessageService.saveNewCopyMessage(task, copyDataJson);
        }
        Integer approvalStatus = MapUtil.getInt(taskVariableData, FlowConstant.LATEST_APPROVAL_STATUS_KEY);
        flowWorkOrderService.updateLatestApprovalStatusByProcessInstanceId(task.getProcessInstanceId(), approvalStatus);
        taskService.complete(task.getId(), taskVariableData);
        flowMessageService.updateFinishedStatusByTaskId(task.getId());
    }

    private String getMultiInstanceAssigneeList(Task task, JSONObject taskVariableData) {
        JSONArray assigneeArray = taskVariableData.getJSONArray(FlowConstant.MULTI_ASSIGNEE_LIST_VAR);
        String assigneeList;
        if (CollUtil.isEmpty(assigneeArray)) {
            FlowTaskExt flowTaskExt = flowTaskExtService.getByProcessDefinitionIdAndTaskId(
                    task.getProcessDefinitionId(), task.getTaskDefinitionKey());
            assigneeList = this.buildMutiSignAssigneeList(flowTaskExt.getOperationListJson());
            if (assigneeList != null) {
                taskVariableData.put(FlowConstant.MULTI_ASSIGNEE_LIST_VAR, StrUtil.split(assigneeList, ','));
            }
        } else {
            assigneeList = CollUtil.join(assigneeArray, ",");
        }
        return assigneeList;
    }

    private JSONObject mergeCopyData(Object copyData, JSONObject passCopyData) {
        // passCopyData是传阅数据，copyData是抄送数据。
        JSONObject resultCopyDataJson = passCopyData;
        if (resultCopyDataJson == null) {
            resultCopyDataJson = JSON.parseObject(copyData.toString());
        } else if (copyData != null) {
            JSONObject copyDataJson = JSON.parseObject(copyData.toString());
            for (Map.Entry<String, Object> entry : copyDataJson.entrySet()) {
                String value = resultCopyDataJson.getString(entry.getKey());
                if (value == null) {
                    resultCopyDataJson.put(entry.getKey(), entry.getValue());
                } else {
                    List<String> list1 = StrUtil.split(value, ",");
                    List<String> list2 = StrUtil.split(entry.getValue().toString(), ",");
                    Set<String> valueSet = new HashSet<>(list1);
                    valueSet.addAll(list2);
                    resultCopyDataJson.put(entry.getKey(), StrUtil.join(",", valueSet));
                }
            }
        }
        this.processMergeCopyData(resultCopyDataJson);
        return resultCopyDataJson;
    }

    private void processMergeCopyData(JSONObject resultCopyDataJson) {
        TokenData tokenData = TokenData.takeFromRequest();
        BaseFlowIdentityExtHelper flowIdentityExtHelper = flowCustomExtFactory.getFlowIdentityExtHelper();
        for (Map.Entry<String, Object> entry : resultCopyDataJson.entrySet()) {
            String type = entry.getKey();
            switch (type) {
                case FlowConstant.GROUP_TYPE_UP_DEPT_POST_LEADER_VAR:
                    Object upLeaderDeptPostId =
                            flowIdentityExtHelper.getUpLeaderDeptPostId(tokenData.getDeptId());
                    entry.setValue(upLeaderDeptPostId);
                    break;
                case FlowConstant.GROUP_TYPE_DEPT_POST_LEADER_VAR:
                    Object leaderDeptPostId =
                            flowIdentityExtHelper.getLeaderDeptPostId(tokenData.getDeptId());
                    entry.setValue(leaderDeptPostId);
                    break;
                case FlowConstant.GROUP_TYPE_SELF_DEPT_POST_VAR:
                    Set<String> selfPostIdSet = new HashSet<>(StrUtil.split(entry.getValue().toString(), ","));
                    Map<String, String> deptPostIdMap =
                            flowIdentityExtHelper.getDeptPostIdMap(tokenData.getDeptId(), selfPostIdSet);
                    String deptPostIdValues = "";
                    if (deptPostIdMap != null) {
                        deptPostIdValues = StrUtil.join(",", deptPostIdMap.values());
                    }
                    entry.setValue(deptPostIdValues);
                    break;
                case FlowConstant.GROUP_TYPE_SIBLING_DEPT_POST_VAR:
                    Set<String> siblingPostIdSet = new HashSet<>(StrUtil.split(entry.getValue().toString(), ","));
                    Map<String, String> siblingDeptPostIdMap =
                            flowIdentityExtHelper.getSiblingDeptPostIdMap(tokenData.getDeptId(), siblingPostIdSet);
                    String siblingDeptPostIdValues = "";
                    if (siblingDeptPostIdMap != null) {
                        siblingDeptPostIdValues = StrUtil.join(",", siblingDeptPostIdMap.values());
                    }
                    entry.setValue(siblingDeptPostIdValues);
                    break;
                case FlowConstant.GROUP_TYPE_UP_DEPT_POST_VAR:
                    Set<String> upPostIdSet = new HashSet<>(StrUtil.split(entry.getValue().toString(), ","));
                    Map<String, String> upDeptPostIdMap =
                            flowIdentityExtHelper.getUpDeptPostIdMap(tokenData.getDeptId(), upPostIdSet);
                    String upDeptPostIdValues = "";
                    if (upDeptPostIdMap != null) {
                        upDeptPostIdValues = StrUtil.join(",", upDeptPostIdMap.values());
                    }
                    entry.setValue(upDeptPostIdValues);
                    break;
                default:
                    break;
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public CallResult verifyAssigneeOrCandidateAndClaim(Task task) {
        String errorMessage;
        String loginName = TokenData.takeFromRequest().getLoginName();
        // 这里必须先执行拾取操作，如果当前用户是候选人，特别是对于分布式场景，更是要先完成候选人的拾取。
        if (task.getAssignee() == null) {
            // 没有指派人
            if (!this.isAssigneeOrCandidate(task)) {
                errorMessage = "数据验证失败，当前用户不是该待办任务的候选人，请刷新后重试！";
                return CallResult.error(errorMessage);
            }
            // 作为候选人主动拾取任务。
            taskService.claim(task.getId(), loginName);
        } else {
            if (!task.getAssignee().equals(loginName)) {
                errorMessage = "数据验证失败，当前用户不是该待办任务的指派人，请刷新后重试！";
                return CallResult.error(errorMessage);
            }
        }
        return CallResult.ok();
    }

    @Override
    public Map<String, Object> initAndGetProcessInstanceVariables(String processDefinitionId) {
        TokenData tokenData = TokenData.takeFromRequest();
        String loginName = tokenData.getLoginName();
        // 设置流程变量。
        Map<String, Object> variableMap = new HashMap<>(4);
        variableMap.put(FlowConstant.PROC_INSTANCE_INITIATOR_VAR, loginName);
        variableMap.put(FlowConstant.PROC_INSTANCE_START_USER_NAME_VAR, loginName);
        List<FlowTaskExt> flowTaskExtList = flowTaskExtService.getByProcessDefinitionId(processDefinitionId);
        boolean hasDeptPostLeader = false;
        boolean hasUpDeptPostLeader = false;
        boolean hasPostCandidateGroup = false;
        for (FlowTaskExt flowTaskExt : flowTaskExtList) {
            if (StrUtil.equals(flowTaskExt.getGroupType(), FlowConstant.GROUP_TYPE_UP_DEPT_POST_LEADER)) {
                hasUpDeptPostLeader = true;
            } else if (StrUtil.equals(flowTaskExt.getGroupType(), FlowConstant.GROUP_TYPE_DEPT_POST_LEADER)) {
                hasDeptPostLeader = true;
            } else if (StrUtil.equals(flowTaskExt.getGroupType(), FlowConstant.GROUP_TYPE_POST)) {
                hasPostCandidateGroup = true;
            }
        }
        // 如果流程图的配置中包含用户身份相关的变量(如：部门领导和上级领导审批)，flowIdentityExtHelper就不能为null。
        // 这个需要子类去实现 BaseFlowIdentityExtHelper 接口，并注册到FlowCustomExtFactory的工厂中。
        BaseFlowIdentityExtHelper flowIdentityExtHelper = flowCustomExtFactory.getFlowIdentityExtHelper();
        if (hasUpDeptPostLeader) {
            Assert.notNull(flowIdentityExtHelper);
            Object upLeaderDeptPostId = flowIdentityExtHelper.getUpLeaderDeptPostId(tokenData.getDeptId());
            if (upLeaderDeptPostId == null) {
                variableMap.put(FlowConstant.GROUP_TYPE_UP_DEPT_POST_LEADER_VAR, null);
            } else {
                variableMap.put(FlowConstant.GROUP_TYPE_UP_DEPT_POST_LEADER_VAR, upLeaderDeptPostId.toString());
            }
        }
        if (hasDeptPostLeader) {
            Assert.notNull(flowIdentityExtHelper);
            Object leaderDeptPostId = flowIdentityExtHelper.getLeaderDeptPostId(tokenData.getDeptId());
            if (leaderDeptPostId == null) {
                variableMap.put(FlowConstant.GROUP_TYPE_DEPT_POST_LEADER_VAR, null);
            } else {
                variableMap.put(FlowConstant.GROUP_TYPE_DEPT_POST_LEADER_VAR, leaderDeptPostId.toString());
            }
        }
        if (hasPostCandidateGroup) {
            Assert.notNull(flowIdentityExtHelper);
            Map<String, Object> postGroupDataMap =
                    this.buildPostCandidateGroupData(flowIdentityExtHelper, flowTaskExtList);
            variableMap.putAll(postGroupDataMap);
        }
        this.buildCopyData(flowTaskExtList, variableMap);
        return variableMap;
    }

    private void buildCopyData(List<FlowTaskExt> flowTaskExtList, Map<String, Object> variableMap) {
        for (FlowTaskExt flowTaskExt : flowTaskExtList) {
            if (StrUtil.isBlank(flowTaskExt.getCopyListJson())) {
                continue;
            }
            List<JSONObject> copyDataList = JSON.parseArray(flowTaskExt.getCopyListJson(), JSONObject.class);
            Map<String, Object> copyDataMap = new HashMap<>(copyDataList.size());
            for (JSONObject copyData : copyDataList) {
                String type = copyData.getString("type");
                String id = copyData.getString("id");
                copyDataMap.put(type, id == null ? "" : id);
            }
            variableMap.put(FlowConstant.COPY_DATA_MAP_PREFIX + flowTaskExt.getTaskId(), JSON.toJSONString(copyDataMap));
        }
    }

    private Map<String, Object> buildPostCandidateGroupData(
            BaseFlowIdentityExtHelper flowIdentityExtHelper, List<FlowTaskExt> flowTaskExtList) {
        Map<String, Object> postVariableMap = MapUtil.newHashMap();
        Set<String> selfPostIdSet = new HashSet<>();
        Set<String> siblingPostIdSet = new HashSet<>();
        Set<String> upPostIdSet = new HashSet<>();
        for (FlowTaskExt flowTaskExt : flowTaskExtList) {
            if (flowTaskExt.getGroupType().equals(FlowConstant.GROUP_TYPE_POST)) {
                Assert.notNull(flowTaskExt.getDeptPostListJson());
                List<FlowTaskPostCandidateGroup> groupDataList =
                        JSONArray.parseArray(flowTaskExt.getDeptPostListJson(), FlowTaskPostCandidateGroup.class);
                for (FlowTaskPostCandidateGroup groupData : groupDataList) {
                    switch (groupData.getType()) {
                        case FlowConstant.GROUP_TYPE_SELF_DEPT_POST_VAR:
                            selfPostIdSet.add(groupData.getPostId());
                            break;
                        case FlowConstant.GROUP_TYPE_SIBLING_DEPT_POST_VAR:
                            siblingPostIdSet.add(groupData.getPostId());
                            break;
                        case FlowConstant.GROUP_TYPE_UP_DEPT_POST_VAR:
                            upPostIdSet.add(groupData.getPostId());
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        postVariableMap.putAll(this.buildSelfPostCandidateGroupData(flowIdentityExtHelper, selfPostIdSet));
        postVariableMap.putAll(this.buildSiblingPostCandidateGroupData(flowIdentityExtHelper, siblingPostIdSet));
        postVariableMap.putAll(this.buildUpPostCandidateGroupData(flowIdentityExtHelper, upPostIdSet));
        return postVariableMap;
    }

    private Map<String, Object> buildSelfPostCandidateGroupData(
            BaseFlowIdentityExtHelper flowIdentityExtHelper, Set<String> selfPostIdSet) {
        Map<String, Object> postVariableMap = MapUtil.newHashMap();
        if (CollUtil.isNotEmpty(selfPostIdSet)) {
            Map<String, String> deptPostIdMap =
                    flowIdentityExtHelper.getDeptPostIdMap(TokenData.takeFromRequest().getDeptId(), selfPostIdSet);
            for (String postId : selfPostIdSet) {
                if (MapUtil.isNotEmpty(deptPostIdMap) && deptPostIdMap.containsKey(postId)) {
                    String deptPostId = deptPostIdMap.get(postId);
                    postVariableMap.put(FlowConstant.SELF_DEPT_POST_PREFIX + postId, deptPostId);
                } else {
                    postVariableMap.put(FlowConstant.SELF_DEPT_POST_PREFIX + postId, "");
                }
            }
        }
        return postVariableMap;
    }

    private Map<String, Object> buildSiblingPostCandidateGroupData(
            BaseFlowIdentityExtHelper flowIdentityExtHelper, Set<String> siblingPostIdSet) {
        Map<String, Object> postVariableMap = MapUtil.newHashMap();
        if (CollUtil.isNotEmpty(siblingPostIdSet)) {
            Map<String, String> siblingDeptPostIdMap =
                    flowIdentityExtHelper.getSiblingDeptPostIdMap(TokenData.takeFromRequest().getDeptId(), siblingPostIdSet);
            for (String postId : siblingPostIdSet) {
                if (MapUtil.isNotEmpty(siblingDeptPostIdMap) && siblingDeptPostIdMap.containsKey(postId)) {
                    String siblingDeptPostId = siblingDeptPostIdMap.get(postId);
                    postVariableMap.put(FlowConstant.SIBLING_DEPT_POST_PREFIX + postId, siblingDeptPostId);
                } else {
                    postVariableMap.put(FlowConstant.SIBLING_DEPT_POST_PREFIX + postId, "");
                }
            }
        }
        return postVariableMap;
    }

    private Map<String, Object> buildUpPostCandidateGroupData(
            BaseFlowIdentityExtHelper flowIdentityExtHelper, Set<String> upPostIdSet) {
        Map<String, Object> postVariableMap = MapUtil.newHashMap();
        if (CollUtil.isNotEmpty(upPostIdSet)) {
            Map<String, String> upDeptPostIdMap =
                    flowIdentityExtHelper.getUpDeptPostIdMap(TokenData.takeFromRequest().getDeptId(), upPostIdSet);
            for (String postId : upPostIdSet) {
                if (MapUtil.isNotEmpty(upDeptPostIdMap) && upDeptPostIdMap.containsKey(postId)) {
                    String upDeptPostId = upDeptPostIdMap.get(postId);
                    postVariableMap.put(FlowConstant.UP_DEPT_POST_PREFIX + postId, upDeptPostId);
                } else {
                    postVariableMap.put(FlowConstant.UP_DEPT_POST_PREFIX + postId, "");
                }
            }
        }
        return postVariableMap;
    }

    @Override
    public boolean isAssigneeOrCandidate(TaskInfo task) {
        String loginName = TokenData.takeFromRequest().getLoginName();
        if (StrUtil.isNotBlank(task.getAssignee())) {
            return StrUtil.equals(loginName, task.getAssignee());
        }
        TaskQuery query = taskService.createTaskQuery();
        this.buildCandidateCondition(query, loginName);
        query.taskId(task.getId());
        return query.active().count() != 0;
    }

    @Override
    public Collection<FlowElement> getProcessAllElements(String processDefinitionId) {
        Process process = repositoryService.getBpmnModel(processDefinitionId).getProcesses().get(0);
        return this.getAllElements(process.getFlowElements(), null);
    }

    @Override
    public boolean isProcessInstanceStarter(String processInstanceId) {
        String loginName = TokenData.takeFromRequest().getLoginName();
        return historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId).startedBy(loginName).count() != 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void setBusinessKeyForProcessInstance(String processInstanceId, Object dataId) {
        runtimeService.updateBusinessKey(processInstanceId, dataId.toString());
    }

    @Override
    public boolean existActiveProcessInstance(String processInstanceId) {
        return runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId).active().count() != 0;
    }

    @Override
    public ProcessInstance getProcessInstance(String processInstanceId) {
        return runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
    }

    @Override
    public Task getProcessInstanceActiveTask(String processInstanceId, String taskId) {
        TaskQuery query = taskService.createTaskQuery().processInstanceId(processInstanceId);
        if (StrUtil.isNotBlank(taskId)) {
            query.taskId(taskId);
        }
        return query.active().singleResult();
    }

    @Override
    public List<Task> getProcessInstanceActiveTaskList(String processInstanceId) {
        return taskService.createTaskQuery().processInstanceId(processInstanceId).list();
    }

    @Override
    public Task getTaskById(String taskId) {
        return taskService.createTaskQuery().taskId(taskId).singleResult();
    }

    @Override
    public MyPageData<Task> getTaskListByUserName(
            String username, String definitionKey, String definitionName, String taskName, MyPageParam pageParam) {
        TaskQuery query = this.createQuery();
        if (StrUtil.isNotBlank(definitionKey)) {
            query.processDefinitionKey(definitionKey);
        }
        if (StrUtil.isNotBlank(definitionName)) {
            query.processDefinitionNameLike("%" + definitionName + "%");
        }
        if (StrUtil.isNotBlank(taskName)) {
            query.taskNameLike("%" + taskName + "%");
        }
        this.buildCandidateCondition(query, username);
        long totalCount = query.count();
        query.orderByTaskCreateTime().desc();
        int firstResult = (pageParam.getPageNum() - 1) * pageParam.getPageSize();
        List<Task> taskList = query.listPage(firstResult, pageParam.getPageSize());
        return new MyPageData<>(taskList, totalCount);
    }

    @Override
    public long getTaskCountByUserName(String username) {
        TaskQuery query = this.createQuery();
        this.buildCandidateCondition(query, username);
        return query.count();
    }

    @Override
    public List<Task> getTaskListByProcessInstanceIds(List<String> processInstanceIdSet) {
        return taskService.createTaskQuery().processInstanceIdIn(processInstanceIdSet).active().list();
    }

    @Override
    public List<ProcessInstance> getProcessInstanceList(Set<String> processInstanceIdSet) {
        return runtimeService.createProcessInstanceQuery().processInstanceIds(processInstanceIdSet).list();
    }

    @Override
    public ProcessDefinition getProcessDefinitionById(String processDefinitionId) {
        return repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
    }

    @Override
    public List<ProcessDefinition> getProcessDefinitionList(Set<String> processDefinitionIdSet) {
        return repositoryService.createProcessDefinitionQuery().processDefinitionIds(processDefinitionIdSet).list();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void suspendProcessDefinition(String processDefinitionId) {
        repositoryService.suspendProcessDefinitionById(processDefinitionId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void activateProcessDefinition(String processDefinitionId) {
        repositoryService.activateProcessDefinitionById(processDefinitionId);
    }

    @Override
    public BpmnModel getBpmnModelByDefinitionId(String processDefinitionId) {
        return repositoryService.getBpmnModel(processDefinitionId);
    }

    @Override
    public boolean isMultiInstanceTask(String processDefinitionId, String taskKey) {
        BpmnModel model = this.getBpmnModelByDefinitionId(processDefinitionId);
        FlowElement flowElement = model.getFlowElement(taskKey);
        if (!(flowElement instanceof UserTask)) {
            return false;
        }
        UserTask userTask = (UserTask) flowElement;
        return userTask.hasMultiInstanceLoopCharacteristics();
    }

    @Override
    public ProcessDefinition getProcessDefinitionByDeployId(String deployId) {
        return repositoryService.createProcessDefinitionQuery().deploymentId(deployId).singleResult();
    }

    @Override
    public void setProcessInstanceVariables(String processInstanceId, Map<String, Object> variableMap) {
        runtimeService.setVariables(processInstanceId, variableMap);
    }

    @Override
    public Object getProcessInstanceVariable(String processInstanceId, String variableName) {
        return runtimeService.getVariable(processInstanceId, variableName);
    }

    @Override
    public List<FlowTaskVo> convertToFlowTaskList(List<Task> taskList) {
        List<FlowTaskVo> flowTaskVoList = new LinkedList<>();
        if (CollUtil.isEmpty(taskList)) {
            return flowTaskVoList;
        }
        Set<String> processDefinitionIdSet = taskList.stream()
                .map(Task::getProcessDefinitionId).collect(Collectors.toSet());
        Set<String> procInstanceIdSet = taskList.stream()
                .map(Task::getProcessInstanceId).collect(Collectors.toSet());
        List<FlowEntryPublish> flowEntryPublishList =
                flowEntryService.getFlowEntryPublishList(processDefinitionIdSet);
        Map<String, FlowEntryPublish> flowEntryPublishMap =
                flowEntryPublishList.stream().collect(Collectors.toMap(FlowEntryPublish::getProcessDefinitionId, c -> c));
        List<ProcessInstance> instanceList = this.getProcessInstanceList(procInstanceIdSet);
        Map<String, ProcessInstance> instanceMap =
                instanceList.stream().collect(Collectors.toMap(ProcessInstance::getId, c -> c));
        List<ProcessDefinition> definitionList = this.getProcessDefinitionList(processDefinitionIdSet);
        Map<String, ProcessDefinition> definitionMap =
                definitionList.stream().collect(Collectors.toMap(ProcessDefinition::getId, c -> c));
        List<FlowWorkOrder> workOrderList =
                flowWorkOrderService.getInList("processInstanceId", procInstanceIdSet);
        Map<String, FlowWorkOrder> workOrderMap =
                workOrderList.stream().collect(Collectors.toMap(FlowWorkOrder::getProcessInstanceId, c -> c));
        for (Task task : taskList) {
            FlowTaskVo flowTaskVo = new FlowTaskVo();
            flowTaskVo.setTaskId(task.getId());
            flowTaskVo.setTaskName(task.getName());
            flowTaskVo.setTaskKey(task.getTaskDefinitionKey());
            flowTaskVo.setTaskFormKey(task.getFormKey());
            flowTaskVo.setTaskStartTime(task.getCreateTime());
            flowTaskVo.setEntryId(flowEntryPublishMap.get(task.getProcessDefinitionId()).getEntryId());
            ProcessDefinition processDefinition = definitionMap.get(task.getProcessDefinitionId());
            flowTaskVo.setProcessDefinitionId(processDefinition.getId());
            flowTaskVo.setProcessDefinitionName(processDefinition.getName());
            flowTaskVo.setProcessDefinitionKey(processDefinition.getKey());
            flowTaskVo.setProcessDefinitionVersion(processDefinition.getVersion());
            ProcessInstance processInstance = instanceMap.get(task.getProcessInstanceId());
            flowTaskVo.setProcessInstanceId(processInstance.getId());
            Object initiator = this.getProcessInstanceVariable(
                    processInstance.getId(), FlowConstant.PROC_INSTANCE_INITIATOR_VAR);
            flowTaskVo.setProcessInstanceInitiator(initiator.toString());
            flowTaskVo.setProcessInstanceStartTime(processInstance.getStartTime());
            flowTaskVo.setBusinessKey(processInstance.getBusinessKey());
            FlowWorkOrder flowWorkOrder = workOrderMap.get(task.getProcessInstanceId());
            if (flowWorkOrder != null) {
                flowTaskVo.setIsDraft(flowWorkOrder.getFlowStatus().equals(FlowTaskStatus.DRAFT));
                flowTaskVo.setWorkOrderCode(flowWorkOrder.getWorkOrderCode());
            }
            flowTaskVoList.add(flowTaskVo);
        }
        Set<String> loginNameSet = flowTaskVoList.stream()
                .map(FlowTaskVo::getProcessInstanceInitiator).collect(Collectors.toSet());
        List<FlowUserInfoVo> flowUserInfos = flowCustomExtFactory
                .getFlowIdentityExtHelper().getUserInfoListByUsernameSet(loginNameSet);
        Map<String, FlowUserInfoVo> userInfoMap =
                flowUserInfos.stream().collect(Collectors.toMap(FlowUserInfoVo::getLoginName, c -> c));
        for (FlowTaskVo flowTaskVo : flowTaskVoList) {
            FlowUserInfoVo userInfo = userInfoMap.get(flowTaskVo.getProcessInstanceInitiator());
            flowTaskVo.setShowName(userInfo.getShowName());
            flowTaskVo.setHeadImageUrl(userInfo.getHeadImageUrl());
        }
        return flowTaskVoList;
    }

    @Override
    public void addProcessInstanceEndListener(BpmnModel bpmnModel, Class<? extends ExecutionListener> listenerClazz) {
        Assert.notNull(listenerClazz);
        Process process = bpmnModel.getMainProcess();
        FlowableListener listener = this.createListener("end", listenerClazz.getName());
        process.getExecutionListeners().add(listener);
    }

    @Override
    public void addExecutionListener(
            FlowElement flowElement,
            Class<? extends ExecutionListener> listenerClazz,
            String event,
            List<FieldExtension> fieldExtensions) {
        Assert.notNull(listenerClazz);
        FlowableListener listener = this.createListener(event, listenerClazz.getName());
        if (fieldExtensions != null) {
            listener.setFieldExtensions(fieldExtensions);
        }
        flowElement.getExecutionListeners().add(listener);
    }

    @Override
    public void addTaskCreateListener(UserTask userTask, Class<? extends TaskListener> listenerClazz) {
        Assert.notNull(listenerClazz);
        FlowableListener listener = this.createListener("create", listenerClazz.getName());
        userTask.getTaskListeners().add(listener);
    }

    @Override
    public HistoricProcessInstance getHistoricProcessInstance(String processInstanceId) {
        return historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
    }

    @Override
    public List<HistoricProcessInstance> getHistoricProcessInstanceList(Set<String> processInstanceIdSet) {
        return historyService.createHistoricProcessInstanceQuery().processInstanceIds(processInstanceIdSet).list();
    }

    @Override
    public MyPageData<HistoricProcessInstance> getHistoricProcessInstanceList(
            String processDefinitionKey,
            String processDefinitionName,
            String startUser,
            String beginDate,
            String endDate,
            MyPageParam pageParam,
            boolean finishedOnly) throws ParseException {
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();
        if (StrUtil.isNotBlank(processDefinitionKey)) {
            query.processDefinitionKey(processDefinitionKey);
        }
        if (StrUtil.isNotBlank(processDefinitionName)) {
            query.processDefinitionName(processDefinitionName);
        }
        if (StrUtil.isNotBlank(startUser)) {
            query.startedBy(startUser);
        }
        if (StrUtil.isNotBlank(beginDate)) {
            SimpleDateFormat sdf = new SimpleDateFormat(MyDateUtil.COMMON_SHORT_DATETIME_FORMAT);
            query.startedAfter(sdf.parse(beginDate));
        }
        if (StrUtil.isNotBlank(endDate)) {
            SimpleDateFormat sdf = new SimpleDateFormat(MyDateUtil.COMMON_SHORT_DATETIME_FORMAT);
            query.startedBefore(sdf.parse(endDate));
        }
        TokenData tokenData = TokenData.takeFromRequest();
        if (tokenData.getTenantId() != null) {
            query.processInstanceTenantId(tokenData.getTenantId().toString());
        } else {
            if (tokenData.getAppCode() == null) {
                query.processInstanceWithoutTenantId();
            } else {
                query.processInstanceTenantId(tokenData.getAppCode());
            }
        }
        if (finishedOnly) {
            query.finished();
        }
        query.orderByProcessInstanceStartTime().desc();
        long totalCount = query.count();
        int firstResult = (pageParam.getPageNum() - 1) * pageParam.getPageSize();
        List<HistoricProcessInstance> instanceList = query.listPage(firstResult, pageParam.getPageSize());
        return new MyPageData<>(instanceList, totalCount);
    }

    @Override
    public MyPageData<HistoricTaskInstance> getHistoricTaskInstanceFinishedList(
            String processDefinitionName,
            String beginDate,
            String endDate,
            MyPageParam pageParam) throws ParseException {
        String loginName = TokenData.takeFromRequest().getLoginName();
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(loginName)
                .finished();
        if (StrUtil.isNotBlank(processDefinitionName)) {
            query.processDefinitionName(processDefinitionName);
        }
        if (StrUtil.isNotBlank(beginDate)) {
            SimpleDateFormat sdf = new SimpleDateFormat(MyDateUtil.COMMON_SHORT_DATETIME_FORMAT);
            query.taskCompletedAfter(sdf.parse(beginDate));
        }
        if (StrUtil.isNotBlank(endDate)) {
            SimpleDateFormat sdf = new SimpleDateFormat(MyDateUtil.COMMON_SHORT_DATETIME_FORMAT);
            query.taskCompletedBefore(sdf.parse(endDate));
        }
        TokenData tokenData = TokenData.takeFromRequest();
        if (tokenData.getTenantId() != null) {
            query.taskTenantId(tokenData.getTenantId().toString());
        } else {
            if (StrUtil.isBlank(tokenData.getAppCode())) {
                query.taskWithoutTenantId();
            } else {
                query.taskTenantId(tokenData.getAppCode());
            }
        }
        query.orderByHistoricTaskInstanceEndTime().desc();
        long totalCount = query.count();
        int firstResult = (pageParam.getPageNum() - 1) * pageParam.getPageSize();
        List<HistoricTaskInstance> instanceList = query.listPage(firstResult, pageParam.getPageSize());
        return new MyPageData<>(instanceList, totalCount);
    }

    @Override
    public List<HistoricActivityInstance> getHistoricActivityInstanceList(String processInstanceId) {
        return historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).list();
    }

    @Override
    public List<HistoricActivityInstance> getHistoricActivityInstanceListOrderByStartTime(String processInstanceId) {
        return historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId).orderByHistoricActivityInstanceStartTime().asc().list();
    }

    @Override
    public HistoricTaskInstance getHistoricTaskInstance(String processInstanceId, String taskId) {
        return historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId).taskId(taskId).singleResult();
    }

    @Override
    public List<HistoricActivityInstance> getHistoricUnfinishedInstanceList(String processInstanceId) {
        return historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId).unfinished().list();
    }

    @MultiDatabaseWriteMethod
    @Transactional(rollbackFor = Exception.class)
    @Override
    public CallResult stopProcessInstance(String processInstanceId, String stopReason, boolean forCancel) {
        //需要先更新状态，以便FlowFinishedListener监听器可以正常的判断流程结束的状态。
        int status = FlowTaskStatus.STOPPED;
        if (forCancel) {
            status = FlowTaskStatus.CANCELLED;
        }
        return this.stopProcessInstance(processInstanceId, stopReason, status);
    }

    @MultiDatabaseWriteMethod
    @Transactional(rollbackFor = Exception.class)
    @Override
    public CallResult stopProcessInstance(String processInstanceId, String stopReason, int status) {
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(processInstanceId).active().list();
        if (CollUtil.isEmpty(taskList)) {
            return CallResult.error("数据验证失败，当前流程尚未开始或已经结束！");
        }
        flowWorkOrderService.updateFlowStatusByProcessInstanceId(processInstanceId, status);
        for (Task task : taskList) {
            String currActivityId = task.getTaskDefinitionKey();
            BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
            FlowNode currFlow = (FlowNode) bpmnModel.getMainProcess().getFlowElement(currActivityId);
            if (currFlow == null) {
                List<SubProcess> subProcessList =
                        bpmnModel.getMainProcess().findFlowElementsOfType(SubProcess.class);
                for (SubProcess subProcess : subProcessList) {
                    FlowElement flowElement = subProcess.getFlowElement(currActivityId);
                    if (flowElement != null) {
                        currFlow = (FlowNode) flowElement;
                        break;
                    }
                }
            }
            EndEvent endEvent = bpmnModel.getMainProcess()
                    .findFlowElementsOfType(EndEvent.class, false).get(0);
            org.springframework.util.Assert.notNull(currFlow, "currFlow can't be NULL");
            if (!(currFlow.getParentContainer().equals(endEvent.getParentContainer()))) {
                throw new FlowOperationException("数据验证失败，不能从子流程直接中止！");
            }
            // 保存原有的输出方向。
            List<SequenceFlow> oriSequenceFlows = Lists.newArrayList();
            oriSequenceFlows.addAll(currFlow.getOutgoingFlows());
            // 清空原有方向。
            currFlow.getOutgoingFlows().clear();
            // 建立新方向。
            SequenceFlow newSequenceFlow = new SequenceFlow();
            String uuid = UUID.randomUUID().toString().replace("-", "");
            newSequenceFlow.setId(uuid);
            newSequenceFlow.setSourceFlowElement(currFlow);
            newSequenceFlow.setTargetFlowElement(endEvent);
            currFlow.setOutgoingFlows(CollUtil.newArrayList(newSequenceFlow));
            // 完成任务并跳转到新方向。
            taskService.complete(task.getId());
            FlowTaskComment taskComment = new FlowTaskComment(task);
            taskComment.setApprovalType(FlowApprovalType.STOP);
            taskComment.setTaskComment(stopReason);
            flowTaskCommentService.saveNew(taskComment);
            // 回复原有输出方向。
            currFlow.setOutgoingFlows(oriSequenceFlows);
        }
        flowMessageService.updateFinishedStatusByProcessInstanceId(processInstanceId);
        return CallResult.ok();
    }

    @MultiDatabaseWriteMethod
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteProcessInstance(String processInstanceId) {
        historyService.deleteHistoricProcessInstance(processInstanceId);
        flowMessageService.removeByProcessInstanceId(processInstanceId);
        FlowWorkOrder workOrder = flowWorkOrderService.getFlowWorkOrderByProcessInstanceId(processInstanceId);
        if (workOrder == null) {
            return;
        }
        FlowEntry flowEntry = flowEntryService.getFlowEntryFromCache(workOrder.getProcessDefinitionKey());
        if (StrUtil.isNotBlank(flowEntry.getExtensionData())) {
            FlowEntryExtensionData extData = JSON.parseObject(flowEntry.getExtensionData(), FlowEntryExtensionData.class);
            if (BooleanUtil.isTrue(extData.getCascadeDeleteBusinessData())) {
                if (workOrder.getOnlineTableId() != null) {
                    // 级联删除在线表单工作流的业务数据。
                    flowCustomExtFactory.getOnlineBusinessDataExtHelper().deleteBusinessData(workOrder);
                } else {
                    // 级联删除路由表单工作流的业务数据。
                    flowCustomExtFactory.getBusinessDataExtHelper().deleteBusinessData(workOrder);
                }
            }
        }
        flowWorkOrderService.removeByProcessInstanceId(processInstanceId);
    }

    @Override
    public Object getTaskVariable(String taskId, String variableName) {
        return taskService.getVariable(taskId, variableName);
    }

    @Override
    public String getTaskVariableStringWithSafe(String taskId, String variableName) {
        try {
            Object v = taskService.getVariable(taskId, variableName);
            if (v == null) {
                return null;
            }
            return v.toString();
        } catch (Exception e) {
            String errorMessage =
                    String.format("Failed to getTaskVariable taskId [%s], variableName [%s]", taskId, variableName);
            log.error(errorMessage, e);
            return null;
        }
    }

    @Override
    public Object getExecutionVariable(String executionId, String variableName) {
        return runtimeService.getVariable(executionId, variableName);
    }

    @Override
    public String getExecutionVariableStringWithSafe(String executionId, String variableName) {
        try {
            Object v = runtimeService.getVariable(executionId, variableName);
            if (v == null) {
                return null;
            }
            return v.toString();
        } catch (Exception e) {
            String errorMessage = String.format(
                    "Failed to getExecutionVariableStringWithSafe executionId [%s], variableName [%s]", executionId, variableName);
            log.error(errorMessage, e);
            return null;
        }
    }

    @Override
    public Object getHistoricProcessInstanceVariable(String processInstanceId, String variableName) {
        HistoricVariableInstance hv = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(processInstanceId)
                .variableName(FlowConstant.GROUP_TYPE_DEPT_POST_LEADER_VAR).singleResult();
        return hv == null ? null : hv.getValue();
    }

    @Override
    public BpmnModel convertToBpmnModel(String bpmnXml) throws XMLStreamException {
        BpmnXMLConverter converter = new BpmnXMLConverter();
        InputStream in = new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8));
        @Cleanup XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(in);
        return converter.convertToBpmnModel(reader);
    }

    @MultiDatabaseWriteMethod
    @Transactional
    @Override
    public CallResult backToRuntimeTask(
            Task task, String targetKey, Integer backType, String reason, JSONObject taskVariableData) {
        try {
            Tuple3<List<String>, List<String>, Boolean> t = this.calculateCurrentAndTargetIds(task, targetKey);
            Map<String, Object> variables = null;
            if (BooleanUtil.isTrue(t.getThird())) {
                variables = new HashMap<>(1);
                FlowRejectData rejectData = new FlowRejectData();
                rejectData.setSourceUser(TokenData.takeFromRequest().getLoginName());
                rejectData.setSourceTaskKey(task.getTaskDefinitionKey());
                rejectData.setTargetTaskKeys(t.getSecond());
                variables.put(FlowConstant.REJECT_TO_SOURCE_DATA_VAR, JSON.toJSONString(rejectData));
            }
            this.doChanageState(task.getProcessInstanceId(), t.getFirst(), t.getSecond(), null, variables);
            FlowTaskComment comment = new FlowTaskComment();
            comment.setTaskId(task.getId());
            comment.setTaskKey(task.getTaskDefinitionKey());
            comment.setTaskName(task.getName());
            switch (backType) {
                case FlowBackType.REJECT:
                    comment.setApprovalType(FlowApprovalType.REJECT);
                    break;
                case FlowBackType.REVOKE:
                    comment.setApprovalType(FlowApprovalType.REVOKE);
                    break;
                default:
                    break;
            }
            comment.setProcessInstanceId(task.getProcessInstanceId());
            comment.setTaskComment(reason);
            comment.setTargetTaskKey(JSON.toJSONString(t.getSecond()));
            flowTaskCommentService.saveNew(comment);
            Integer approvalStatus = MapUtil.getInt(taskVariableData, FlowConstant.LATEST_APPROVAL_STATUS_KEY);
            flowWorkOrderService.updateLatestApprovalStatusByProcessInstanceId(task.getProcessInstanceId(), approvalStatus);
            return CallResult.ok();
        } catch (MyRuntimeException e) {
            return CallResult.error(e.getMessage());
        }
    }

    private Tuple3<List<String>, List<String>, Boolean> calculateCurrentAndTargetIds(Task task, String targetKey) {
        List<Task> taskList = this.getProcessInstanceActiveTaskList(task.getProcessInstanceId());
        List<String> targetIds;
        if (StrUtil.isNotBlank(targetKey)) {
            if (taskList.stream().anyMatch(t -> t.getTaskDefinitionKey().equals(targetKey))) {
                throw new MyRuntimeException("数据验证失败，不能驳回到其他待办任务！");
            }
            List<HistoricActivityInstance> histTaskList =
                    this.getHistoricActivityInstanceList(task.getProcessInstanceId());
            if (histTaskList.stream().noneMatch(t -> t.getActivityId().equals(targetKey))) {
                throw new MyRuntimeException("数据验证失败，不能驳回到没有执行过的流程任务节点！");
            }
            targetIds = CollUtil.newArrayList(targetKey);
        } else {
            TypedCallResult<List<String>> callResult = this.deduceBackTargetIds(task);
            if (!callResult.isSuccess()) {
                throw new MyRuntimeException(callResult.getErrorMessage());
            }
            targetIds = callResult.getData();
        }
        FlowTaskExt taskExt = flowTaskExtService
                .getByProcessDefinitionIdAndTaskId(task.getProcessDefinitionId(), task.getTaskDefinitionKey());
        FlowUserTaskExtData extData = JSON.parseObject(taskExt.getExtraDataJson(), FlowUserTaskExtData.class);
        if (this.isRejectBackToSourceUserTask(extData) && (targetKey != null || targetIds.size() == 1)) {
            return new Tuple3<>(CollUtil.newArrayList(task.getTaskDefinitionKey()), targetIds, true);
        }
        List<String> currentIds = taskList.stream().map(Task::getTaskDefinitionKey).collect(Collectors.toList());
        if (targetKey == null) {
            currentIds = this.getChildActivitiIdList(task.getProcessDefinitionId(), targetIds.get(0), currentIds);
        }
        return new Tuple3<>(currentIds, targetIds, false);
    }

    private boolean isRejectBackToSourceUserTask(FlowUserTaskExtData extData) {
        return extData != null && StrUtil.equals(extData.getRejectType(), FlowUserTaskExtData.REJECT_TYPE_BACK_TO_SOURCE);
    }

    private UserTask findUserTaskByTask(String processDefinitionId, String taskKey) {
        ProcessDefinition processDefinition = this.getProcessDefinitionById(processDefinitionId);
        Collection<FlowElement> allElements = this.getProcessAllElements(processDefinition.getId());
        return (UserTask) this.findFlement(allElements, taskKey);
    }

    private TypedCallResult<List<String>> deduceBackTargetIds(Task task) {
        UserTask source = this.findUserTaskByTask(task.getProcessDefinitionId(), task.getTaskDefinitionKey());
        List<UserTask> parentUserTaskList = this.getParentUserTaskList(source, new HashSet<>(), new ArrayList<>());
        if (CollUtil.isEmpty(parentUserTaskList)) {
            return TypedCallResult.error("数据验证失败，当前节点为初始任务节点，不能驳回！");
        }
        // 获取上一步途径过的用户任务Ids
        UserTask targetUserTask = this.getBackParentUserTask(parentUserTaskList, task);
        if (targetUserTask == null) {
            return TypedCallResult.error("数据验证失败，不能获取驳回节点！");
        }
        String parallelGatewayId = null;
        List<SequenceFlow> outgoingFlows = targetUserTask.getOutgoingFlows();
        for (SequenceFlow outgoingFlow : outgoingFlows) {
            if (outgoingFlow.getTargetFlowElement() instanceof ParallelGateway) {
                parallelGatewayId = outgoingFlow.getTargetFlowElement().getId();
                break;
            }
        }
        if (parallelGatewayId == null) {
            return TypedCallResult.ok(CollUtil.newArrayList(targetUserTask.getId()));
        }
        List<UserTask> targetUserTasks = CollUtil.newArrayList(targetUserTask);
        for (UserTask parentTask : parentUserTaskList) {
            if (parentTask == targetUserTask) {
                continue;
            }
            for (SequenceFlow outgoingFlow : parentTask.getOutgoingFlows()) {
                if (StrUtil.equals(outgoingFlow.getTargetFlowElement().getId(), parallelGatewayId)) {
                    targetUserTasks.add(parentTask);
                }
            }
        }
        List<String> targetIds = targetUserTasks.stream().map(UserTask::getId).collect(Collectors.toList());
        return TypedCallResult.ok(targetIds);
    }

    @Transactional
    @Override
    public CallResult backToRuntimeTaskAndTransfer(
            Task task, String targetKey, Integer backType, String reason, String delegateAssignee) {
        CallResult result = this.backToRuntimeTask(task, targetKey, backType, reason, null);
        if (!result.isSuccess()) {
            return result;
        }
        FlowTaskComment flowTaskComment = new FlowTaskComment();
        flowTaskComment.setDelegateAssignee(delegateAssignee);
        flowTaskComment.setApprovalType(FlowApprovalType.INTERVENE);
        Task targetTask = this.getProcessInstanceActiveTaskList(task.getProcessInstanceId()).get(0);
        this.transferTo(targetTask, flowTaskComment);
        return result;
    }

    @Override
    public List<UserTask> getRejectCandidateUserTaskList(Task task) {
        List<UserTask> userTaskList = null;
        ProcessDefinition processDefinition = this.getProcessDefinitionById(task.getProcessDefinitionId());
        Collection<FlowElement> allElements = this.getProcessAllElements(processDefinition.getId());
        if (CollUtil.isEmpty(allElements)) {
            return userTaskList;
        }
        // 获取当前任务节点元素
        UserTask source = null;
        for (FlowElement flowElement : allElements) {
            if (flowElement.getId().equals(task.getTaskDefinitionKey())) {
                source = (UserTask) flowElement;
                break;
            }
        }
        // 获取节点的所有路线
        List<List<UserTask>> roads = this.findRoad(source, null, null, null);
        // 可回退的节点列表
        userTaskList = new ArrayList<>();
        for (List<UserTask> road : roads) {
            if (CollUtil.isEmpty(userTaskList)) {
                // 还没有可回退节点直接添加
                userTaskList = road;
            } else {
                // 如果已有回退节点，则比对取交集部分
                userTaskList.retainAll(road);
            }
        }
        return userTaskList;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void transferTo(Task task, FlowTaskComment flowTaskComment) {
        List<String> transferUserList = StrUtil.split(flowTaskComment.getDelegateAssignee(), ",");
        for (String transferUser : transferUserList) {
            if (transferUser.equals(FlowConstant.START_USER_NAME_VAR)) {
                String startUser = this.getProcessInstanceVariable(
                        task.getProcessInstanceId(), FlowConstant.PROC_INSTANCE_START_USER_NAME_VAR).toString();
                String newDelegateAssignee = StrUtil.replace(
                        flowTaskComment.getDelegateAssignee(), FlowConstant.START_USER_NAME_VAR, startUser);
                flowTaskComment.setDelegateAssignee(newDelegateAssignee);
                transferUserList = StrUtil.split(flowTaskComment.getDelegateAssignee(), ",");
                break;
            }
        }
        taskService.unclaim(task.getId());
        FlowTaskExt taskExt = flowTaskExtService.getByProcessDefinitionIdAndTaskId(
                task.getProcessDefinitionId(), task.getTaskDefinitionKey());
        if (StrUtil.isNotBlank(taskExt.getCandidateUsernames())) {
            List<String> candidateUsernames = this.getCandidateUsernames(taskExt, task.getId());
            if (CollUtil.isNotEmpty(candidateUsernames)) {
                for (String username : candidateUsernames) {
                    taskService.deleteCandidateUser(task.getId(), username);
                }
            }
        } else {
            this.removeCandidateGroup(taskExt, task);
        }
        transferUserList.forEach(u -> taskService.addCandidateUser(task.getId(), u));
        flowTaskComment.fillWith(task);
        flowTaskCommentService.saveNew(flowTaskComment);
    }

    @Override
    public List<String> getCandidateUsernames(FlowTaskExt flowTaskExt, String taskId) {
        if (StrUtil.isBlank(flowTaskExt.getCandidateUsernames())) {
            return Collections.emptyList();
        }
        if (!StrUtil.equals(flowTaskExt.getCandidateUsernames(), "${" + FlowConstant.TASK_APPOINTED_ASSIGNEE_VAR + "}")) {
            return StrUtil.split(flowTaskExt.getCandidateUsernames(), ",");
        }
        Object candidateUsernames = getTaskVariableStringWithSafe(taskId, FlowConstant.TASK_APPOINTED_ASSIGNEE_VAR);
        return candidateUsernames == null ? null : StrUtil.split(candidateUsernames.toString(), ",");
    }

    @Override
    public Tuple2<Set<String>, Set<String>> getDeptPostIdAndPostIds(
            FlowTaskExt flowTaskExt, String processInstanceId, boolean historic) {
        Set<String> postIdSet = new LinkedHashSet<>();
        Set<String> deptPostIdSet = new LinkedHashSet<>();
        if (StrUtil.equals(flowTaskExt.getGroupType(), FlowConstant.GROUP_TYPE_UP_DEPT_POST_LEADER)) {
            Object v = this.getProcessInstanceVariable(
                    processInstanceId, FlowConstant.GROUP_TYPE_UP_DEPT_POST_LEADER_VAR, historic);
            if (ObjectUtil.isNotEmpty(v)) {
                deptPostIdSet.add(v.toString());
            }
        } else if (StrUtil.equals(flowTaskExt.getGroupType(), FlowConstant.GROUP_TYPE_DEPT_POST_LEADER)) {
            Object v = this.getProcessInstanceVariable(
                    processInstanceId, FlowConstant.GROUP_TYPE_DEPT_POST_LEADER_VAR, historic);
            if (ObjectUtil.isNotEmpty(v)) {
                deptPostIdSet.add(v.toString());
            }
        } else if (StrUtil.equals(flowTaskExt.getGroupType(), FlowConstant.GROUP_TYPE_POST)
                && StrUtil.isNotBlank(flowTaskExt.getDeptPostListJson())) {
            this.buildDeptPostIdAndPostIdsForPost(flowTaskExt, processInstanceId, historic, postIdSet, deptPostIdSet);
        }
        return new Tuple2<>(deptPostIdSet, postIdSet);
    }

    @Override
    public AnalyzedNode analyzeBpmnRoads(String processDefinitionId) {
        ProcessDefinition processDefinition = this.getProcessDefinitionById(processDefinitionId);
        Collection<FlowElement> allElements = this.getProcessAllElements(processDefinition.getId());
        StartEvent startEvent = (StartEvent) allElements.stream()
                .filter(StartEvent.class::isInstance).findFirst().orElse(null);
        org.springframework.util.Assert.notNull(startEvent, "StartEvent can't be NULL.");
        List<List<FlowElement>> roads =
                this.findForwardRoad(startEvent, new LinkedList<>(), new HashSet<>(), new LinkedList<>());
        List<List<FlowElement>> normalizedRoads = new LinkedList<>();
        for (List<FlowElement> road : roads) {
            List<FlowElement> normalizedRoad = new LinkedList<>();
            for (FlowElement el : road) {
                if (!normalizedRoad.contains(el)) {
                    normalizedRoad.add(el);
                }
            }
            normalizedRoads.add(normalizedRoad);
        }
        roads = normalizedRoads;
        List<GatewayExt> gatewayExts = this.calculateGatewayPassCount(roads);
        Map<String, AnalyzedNode> gatewayNodeMap = this.analyzeGatewayPair(gatewayExts, new HashSet<>());
        this.normalizeGatewayParentRelation(gatewayNodeMap, roads);
        AnalyzedNode rootNode = new AnalyzedNode();
        rootNode.setStartId(startEvent.getId());
        rootNode.setEndId("end");
        rootNode.setStartEvent(true);
        rootNode.setParallelGateway(false);
        for (AnalyzedNode node : gatewayNodeMap.values()) {
            if (node.getParentId() == null) {
                node.setParentId(startEvent.getId());
                rootNode.getChildList().add(node);
            }
        }
        Map<String, AnalyzedNode> fullNodeMap = new HashMap<>(gatewayNodeMap);
        fullNodeMap.put(rootNode.getStartId(), rootNode);
        List<List<String>> allRoads = new LinkedList<>();
        roads.forEach(road -> allRoads.add(road.stream().map(FlowElement::getId).collect(Collectors.toList())));
        rootNode.setAllRoads(allRoads);
        this.analyzeRoadPath(fullNodeMap, roads, startEvent);
        return rootNode;
    }

    @Transactional
    @Override
    public CallResult freeJumpTo(Task task, String targetKey, String comment, String delegateAssignee) {
        CallResult jumpResult = this.doJumpTo(task, targetKey, delegateAssignee);
        if (!jumpResult.isSuccess()) {
            return jumpResult;
        }
        FlowTaskComment taskComment = new FlowTaskComment();
        taskComment.fillWith(task);
        taskComment.setApprovalType(FlowApprovalType.FREE_JUMP);
        taskComment.setTaskComment(comment);
        taskComment.setTargetTaskKey(targetKey);
        flowTaskCommentService.saveNew(taskComment);
        return jumpResult;
    }

    @Override
    public CallResult interveneTo(Task task, String targetKey, String comment, String delegateAssignee) {
        CallResult jumpResult = this.doJumpTo(task, targetKey, delegateAssignee);
        if (!jumpResult.isSuccess()) {
            return jumpResult;
        }
        FlowTaskComment taskComment = new FlowTaskComment();
        taskComment.fillWith(task);
        taskComment.setTargetTaskKey(targetKey);
        taskComment.setTaskComment(comment);
        taskComment.setApprovalType(FlowApprovalType.INTERVENE);
        taskComment.setDelegateAssignee(delegateAssignee);
        flowTaskCommentService.saveNew(taskComment);
        return jumpResult;
    }

    @Override
    public List<String> getChildActivitiIdList(String processDefinitionId, String taskKey, List<String> allActivitiIds) {
        if (StrUtil.isBlank(taskKey) || CollUtil.isEmpty(allActivitiIds)) {
            return new LinkedList<>();
        }
        FlowElement source = this.findUserTaskByTask(processDefinitionId, taskKey);
        List<UserTask> childList = this.getChildUserTaskList(source, new HashSet<>(), new ArrayList<>());
        Set<String> childIds = childList.stream().map(UserTask::getId).collect(Collectors.toSet());
        return allActivitiIds.stream().filter(childIds::contains).collect(Collectors.toList());
    }

    @Override
    public Map<String, UserTask> getAllUserTaskMap(String processDefinitionId) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        Process process = bpmnModel.getProcesses().get(0);
        return process.findFlowElementsOfType(UserTask.class)
                .stream().collect(Collectors.toMap(UserTask::getId, a -> a, (k1, k2) -> k1));
    }

    private CallResult doJumpTo(Task task, String targetKey, String delegateAssignee) {
        List<FlowEntryPublish> flowEntryPublishList =
                flowEntryService.getFlowEntryPublishList(CollUtil.newHashSet(task.getProcessDefinitionId()));
        AnalyzedNode rootNode = JSON.parseObject(flowEntryPublishList.get(0).getAnalyzedNodeJson(), AnalyzedNode.class);
        Map<String, AnalyzedNode> fullNodeMap = this.expandToAnalyzedNodeMap(rootNode);
        UserTaskInfo sourceUserTaskInfo = this.deduceUserTaskInfo(fullNodeMap.values(), task.getTaskDefinitionKey());
        org.springframework.util.Assert.notNull(sourceUserTaskInfo, "sourceUserTaskInfo can't be NULL.");
        if (sourceUserTaskInfo.isMultiInstance()) {
            return CallResult.error("数据验证失败，跳转的源节点不能是多实例会签节点！");
        }
        UserTaskInfo targetUserTaskInfo = this.deduceUserTaskInfo(fullNodeMap.values(), targetKey);
        if (targetUserTaskInfo == null) {
            return CallResult.error("数据验证失败，跳转的目标节点不存在！");
        }
        if (targetUserTaskInfo.isMultiInstance()) {
            return CallResult.error("数据验证失败，跳转的目标节点不能是多实例会签节点！");
        }
        AnalyzedNode sourceNode = fullNodeMap.get(sourceUserTaskInfo.getNodeId());
        AnalyzedNode targetNode = fullNodeMap.get(targetUserTaskInfo.getNodeId());
        if (sourceNode == targetNode) {
            if (sourceNode.isParallelGateway() &&
                    sourceUserTaskInfo.getRoadIndex() != targetUserTaskInfo.getRoadIndex()) {
                return CallResult.error("数据验证失败，相同并行网关内不同线路的任务不能直接跳转！");
            }
            this.doChanageState(task.getProcessInstanceId(),
                    CollUtil.newArrayList(task.getTaskDefinitionKey()), CollUtil.newArrayList(targetKey), delegateAssignee, null);
            return CallResult.ok();
        }
        if (!this.placedWithinParallelGateway(fullNodeMap, targetNode)) {
            if (!this.placedWithinParallelGateway(fullNodeMap, sourceNode)) {
                this.doChanageState(task.getProcessInstanceId(),
                        CollUtil.newArrayList(task.getTaskDefinitionKey()), CollUtil.newArrayList(targetKey), delegateAssignee, null);
            } else {
                List<String> runActivityIds = this.getCurrentActivityIds(task.getProcessInstanceId());
                this.doChanageState(task.getProcessInstanceId(),
                        runActivityIds, CollUtil.newArrayList(targetKey), delegateAssignee, null);
            }
            return CallResult.ok();
        }
        AnalyzedNode targetParanetNode = this.deduceParentParallelGatewayNode(fullNodeMap, targetNode);
        AnalyzedNode sourceParentNode = this.deduceParentParallelGatewayNode(fullNodeMap, sourceNode);
        if (this.isParentOrEqualNode(targetParanetNode, sourceParentNode, fullNodeMap)) {
            List<String> runActivityIds = this.getCurrentActivityIds(task.getProcessInstanceId());
            List<String> canJumpedActivityIds = runActivityIds.stream()
                    .filter(id -> this.canFlowTo(id, targetKey, rootNode.getAllRoads())).collect(Collectors.toList());
            if (!CollUtil.contains(canJumpedActivityIds, task.getTaskDefinitionKey())) {
                return CallResult.error("数据验证失败，原任务节点和目标任务节点位于不同的执行路径，不能直接跳转！");
            }
            this.doChanageState(task.getProcessInstanceId(),
                    canJumpedActivityIds, CollUtil.newArrayList(targetKey), delegateAssignee, null);
            return CallResult.ok();
        }
        if (!StrUtil.equals(targetNode.getParentId(), sourceNode.getStartId())) {
            return CallResult.error("数据验证失败，不能跳转到没有直接上下级关系的并行网关内的目标任务！");
        }
        List<String> targetIds = this.getTargetActivityIds(targetNode, targetKey);
        doChanageState(task.getProcessInstanceId(),
                CollUtil.newArrayList(task.getTaskDefinitionKey()), targetIds, delegateAssignee, null);
        return CallResult.ok();
    }

    private void doChanageState(
            String processInstanceId,
            List<String> currentIds,
            List<String> targetIds,
            String delegateAssignee,
            Map<String, Object> variables) {
        if (ObjectUtil.hasEmpty(currentIds, targetIds)) {
            throw new MyRuntimeException("跳转的源节点和任务节点数量均不能为空！");
        }
        if (currentIds.size() > 1 && targetIds.size() > 1) {
            throw new MyRuntimeException("目前暂不支持源节点和目标节点同时为多个的场景！");
        }
        ChangeActivityStateBuilder builder =
                runtimeService.createChangeActivityStateBuilder().processInstanceId(processInstanceId);
        if (targetIds.size() == 1) {
            if (currentIds.size() == 1) {
                builder.moveActivityIdTo(currentIds.get(0), targetIds.get(0));
            } else {
                builder.moveActivityIdsToSingleActivityId(currentIds, targetIds.get(0));
            }
        } else {
            builder.moveSingleActivityIdToActivityIds(currentIds.get(0), targetIds);
        }
        if (StrUtil.isNotBlank(delegateAssignee)) {
            targetIds.forEach(targetId -> {
                builder.localVariable(targetId, FlowConstant.TASK_APPOINTED_ASSIGNEE_VAR, delegateAssignee);
                builder.localVariable(targetId, FlowConstant.DELEGATE_ASSIGNEE_VAR, delegateAssignee);
            });
        } else {
            targetIds.forEach(targetId -> {
                FlowTaskComment comment = flowTaskCommentService.getLatestFlowTaskComment(processInstanceId, targetId);
                if (comment != null && StrUtil.isNotBlank(comment.getCreateLoginName())) {
                    builder.localVariable(targetId, FlowConstant.TASK_APPOINTED_ASSIGNEE_VAR, comment.getCreateLoginName());
                }
            });
        }
        if (MapUtil.isNotEmpty(variables)) {
            targetIds.forEach(targetId -> builder.localVariables(targetId, variables));
        }
        builder.changeState();
    }

    private List<String> getTargetActivityIds(AnalyzedNode targetNode, String targetKey) {
        List<String> targetIds = CollUtil.newLinkedList(targetKey);
        for (List<UserTaskInfo> road : targetNode.getUserTaskRoads()) {
            boolean found = false;
            for (UserTaskInfo userTaskInfo : road) {
                if (userTaskInfo.getTaskId().equals(targetKey)) {
                    found = true;
                    break;
                }
            }
            if (!found && CollUtil.isNotEmpty(road)) {
                targetIds.add(road.get(0).getTaskId());
            }
        }
        return targetIds;
    }

    private List<String> getCurrentActivityIds(String processInstanceId) {
        List<Execution> runExecutionList =
                runtimeService.createExecutionQuery().processInstanceId(processInstanceId).list();
        return runExecutionList.stream()
                .filter(c -> StrUtil.isNotBlank(c.getActivityId()))
                .map(Execution::getActivityId).collect(Collectors.toList());
    }

    private boolean isParentOrEqualNode(
            AnalyzedNode parentNode, AnalyzedNode childNode, Map<String, AnalyzedNode> fullNodeMap) {
        if (parentNode == childNode) {
            return true;
        }
        if (parentNode == null || childNode == null || childNode.getParentId() == null) {
            return false;
        }
        if (childNode.getParentId().equals(parentNode.getStartId())) {
            return true;
        }
        return this.isParentOrEqualNode(parentNode, fullNodeMap.get(childNode.getParentId()), fullNodeMap);
    }

    private boolean canFlowTo(String sourceId, String targetId, List<List<String>> allRoads) {
        if (CollUtil.isEmpty(allRoads)) {
            return false;
        }
        for (List<String> road : allRoads) {
            if (road.contains(sourceId) && road.contains(targetId)) {
                return true;
            }
        }
        return false;
    }

    private void calculateParentParallelNodes(
            Map<String, AnalyzedNode> fullNodeMap, AnalyzedNode node, List<AnalyzedNode> resultList) {
        if (node.isParallelGateway()) {
            resultList.add(node);
        }
        if (StrUtil.isBlank(node.getParentId())) {
            return;
        }
        this.calculateParentParallelNodes(fullNodeMap, fullNodeMap.get(node.getParentId()), resultList);
    }

    private AnalyzedNode deduceParentParallelGatewayNode(Map<String, AnalyzedNode> fullNodeMap, AnalyzedNode node) {
        if (node.isParallelGateway()) {
            return node;
        }
        if (StrUtil.isBlank(node.getParentId())) {
            return null;
        }
        return this.deduceParentParallelGatewayNode(fullNodeMap, fullNodeMap.get(node.getParentId()));
    }

    private boolean placedWithinParallelGateway(Map<String, AnalyzedNode> fullNodeMap, AnalyzedNode node) {
        if (node.isParallelGateway()) {
            return true;
        }
        if (StrUtil.isBlank(node.getParentId())) {
            return false;
        }
        return this.placedWithinParallelGateway(fullNodeMap, fullNodeMap.get(node.getParentId()));
    }

    private UserTaskInfo deduceUserTaskInfo(Collection<AnalyzedNode> nodeList, String taskId) {
        for (AnalyzedNode node : nodeList) {
            if (CollUtil.isNotEmpty(node.getUserTaskRoads())) {
                for (List<UserTaskInfo> road : node.getUserTaskRoads()) {
                    UserTaskInfo foundInfo =
                            road.stream().filter(info -> info.getTaskId().equals(taskId)).findFirst().orElse(null);
                    if (foundInfo != null) {
                        return foundInfo;
                    }
                }
            }
        }
        return null;
    }

    private Map<String, AnalyzedNode> expandToAnalyzedNodeMap(AnalyzedNode node) {
        Map<String, AnalyzedNode> fullNodeMap = MapUtil.newHashMap();
        fullNodeMap.put(node.getStartId(), node);
        if (CollUtil.isNotEmpty(node.getChildList())) {
            for (AnalyzedNode childNode : node.getChildList()) {
                fullNodeMap.putAll(this.expandToAnalyzedNodeMap(childNode));
            }
        }
        return fullNodeMap;
    }

    private void removeCandidateGroup(FlowTaskExt taskExt, Task task) {
        if (StrUtil.isNotBlank(taskExt.getDeptIds())) {
            for (String deptId : StrUtil.split(taskExt.getDeptIds(), ",")) {
                taskService.deleteCandidateGroup(task.getId(), deptId);
            }
        }
        if (StrUtil.isNotBlank(taskExt.getRoleIds())) {
            for (String roleId : StrUtil.split(taskExt.getRoleIds(), ",")) {
                taskService.deleteCandidateGroup(task.getId(), roleId);
            }
        }
        Tuple2<Set<String>, Set<String>> tuple2 =
                getDeptPostIdAndPostIds(taskExt, task.getProcessInstanceId(), false);
        if (CollUtil.isNotEmpty(tuple2.getFirst())) {
            for (String deptPostId : tuple2.getFirst()) {
                taskService.deleteCandidateGroup(task.getId(), deptPostId);
            }
        }
        if (CollUtil.isNotEmpty(tuple2.getSecond())) {
            for (String postId : tuple2.getSecond()) {
                taskService.deleteCandidateGroup(task.getId(), postId);
            }
        }
    }

    private void buildDeptPostIdAndPostIdsForPost(
            FlowTaskExt flowTaskExt,
            String processInstanceId,
            boolean historic,
            Set<String> postIdSet,
            Set<String> deptPostIdSet) {
        List<FlowTaskPostCandidateGroup> groupDataList =
                JSON.parseArray(flowTaskExt.getDeptPostListJson(), FlowTaskPostCandidateGroup.class);
        for (FlowTaskPostCandidateGroup groupData : groupDataList) {
            switch (groupData.getType()) {
                case FlowConstant.GROUP_TYPE_ALL_DEPT_POST_VAR:
                    postIdSet.add(groupData.getPostId());
                    break;
                case FlowConstant.GROUP_TYPE_DEPT_POST_VAR:
                    deptPostIdSet.add(groupData.getDeptPostId());
                    break;
                case FlowConstant.GROUP_TYPE_SELF_DEPT_POST_VAR:
                    Object v = this.getProcessInstanceVariable(
                            processInstanceId, FlowConstant.SELF_DEPT_POST_PREFIX + groupData.getPostId(), historic);
                    if (ObjectUtil.isNotEmpty(v)) {
                        deptPostIdSet.add(v.toString());
                    }
                    break;
                case FlowConstant.GROUP_TYPE_UP_DEPT_POST_VAR:
                    Object v2 = this.getProcessInstanceVariable(
                            processInstanceId, FlowConstant.UP_DEPT_POST_PREFIX + groupData.getPostId(), historic);
                    if (ObjectUtil.isNotEmpty(v2)) {
                        deptPostIdSet.add(v2.toString());
                    }
                    break;
                case FlowConstant.GROUP_TYPE_SIBLING_DEPT_POST_VAR:
                    Object v3 = this.getProcessInstanceVariable(
                            processInstanceId, FlowConstant.SIBLING_DEPT_POST_PREFIX + groupData.getPostId(), historic);
                    if (ObjectUtil.isNotEmpty(v3)) {
                        deptPostIdSet.addAll(StrUtil.split(v3.toString(), ",")
                                .stream().filter(StrUtil::isNotBlank).collect(Collectors.toList()));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private Object getProcessInstanceVariable(String processInstanceId, String variableName, boolean historic) {
        if (historic) {
            return getHistoricProcessInstanceVariable(processInstanceId, variableName);
        }
        return getProcessInstanceVariable(processInstanceId, variableName);
    }

    private void analyzeRoadPath(
            Map<String, AnalyzedNode> fullNodeMap, List<List<FlowElement>> roads, StartEvent startEvent) {
        Deque<String> stack = new LinkedList<>();
        stack.push(startEvent.getId());
        for (List<FlowElement> road : roads) {
            int index = roads.indexOf(road);
            road.forEach(el -> {
                if (el instanceof Gateway) {
                    if (fullNodeMap.containsKey(el.getId())) {
                        stack.push(el.getId());
                    } else {
                        this.safePollParallelGateway(fullNodeMap, stack);
                    }
                } else if (el instanceof UserTask) {
                    String lastElementId = stack.peek();
                    if (lastElementId != null) {
                        AnalyzedNode node = fullNodeMap.get(lastElementId);
                        node.getAllUserTasks().add("road" + index + "---" + el.getId());
                    }
                }
            });
        }
        this.analyzeRoadAndSplitToPath(fullNodeMap, roads);
    }

    private void analyzeRoadAndSplitToPath(Map<String, AnalyzedNode> fullGatewayMap, List<List<FlowElement>> roads) {
        Map<String, FlowElement> allFlowElementMap = new HashMap<>(20);
        roads.forEach(road -> allFlowElementMap.putAll(road.stream()
                .filter(UserTask.class::isInstance).collect(Collectors.toMap(FlowElement::getId, el -> el))));
        fullGatewayMap.values().forEach(node -> {
            Set<String> set = new HashSet<>();
            for (int i = 0; i < roads.size(); i++) {
                StringBuilder sb = new StringBuilder(128);
                for (String el : node.getAllUserTasks()) {
                    String prefix = "road" + i + "---";
                    if (StrUtil.startWith(el, prefix)) {
                        sb.append(StrUtil.removePrefix(el, prefix)).append(",");
                    }
                }
                set.add(sb.toString());
            }
            set = set.stream().filter(StrUtil::isNotBlank).collect(Collectors.toSet());
            int index = 0;
            for (String s : set) {
                List<String> idList = StrUtil.split(s, ",")
                        .stream().filter(StrUtil::isNotBlank).collect(Collectors.toList());
                List<UserTaskInfo> userTaskRoad = new LinkedList<>();
                for (String id : idList) {
                    FlowElement el = allFlowElementMap.get(id);
                    UserTask userTask = (UserTask) el;
                    userTaskRoad.add(new UserTaskInfo(
                            userTask.getId(), userTask.hasMultiInstanceLoopCharacteristics(), node.getStartId(), index));
                }
                index++;
                node.getUserTaskRoads().add(userTaskRoad);
            }
        });
    }

    private void normalizeGatewayParentRelation(Map<String, AnalyzedNode> fullGatewayMap, List<List<FlowElement>> roads) {
        for (List<FlowElement> road : roads) {
            Deque<String> stack = new LinkedList<>();
            road.stream().filter(Gateway.class::isInstance).forEach(el -> {
                if (fullGatewayMap.containsKey(el.getId())) {
                    String lastElementId = stack.peek();
                    if (lastElementId != null) {
                        AnalyzedNode parentNode = fullGatewayMap.get(lastElementId);
                        if (!lastElementId.equals(el.getId())) {
                            fullGatewayMap.get(el.getId()).setParentId(parentNode.getStartId());
                        }
                    }
                    stack.push(el.getId());
                } else {
                    this.safePollParallelGateway(fullGatewayMap, stack);
                }
            });
        }
        fullGatewayMap.values().stream().filter(node -> node.getParentId() != null).forEach(node -> {
            AnalyzedNode parentNode = fullGatewayMap.get(node.getParentId());
            parentNode.getChildList().add(node);
        });
    }

    private void safePollParallelGateway(Map<String, AnalyzedNode> fullGatewayMap, Deque<String> stack) {
        AnalyzedNode lastNode;
        do {
            String lastElementId = stack.poll();
            if (lastElementId == null) {
                break;
            }
            lastNode = fullGatewayMap.get(lastElementId);
        } while (!lastNode.isParallelGateway());
    }

    private Map<String, AnalyzedNode> analyzeGatewayPair(List<GatewayExt> gatewayExts, Set<String> scannedSet) {
        Map<String, AnalyzedNode> fullGatewayMap = MapUtil.newHashMap();
        int index = 0;
        for (GatewayExt ext : gatewayExts) {
            index++;
            if (scannedSet.contains(ext.gateway.getId())) {
                continue;
            }
            AnalyzedNode node = new AnalyzedNode();
            node.setStartId(ext.gateway.getId());
            node.setParallelGateway(ext.getGateway() instanceof ParallelGateway);
            fullGatewayMap.put(node.getStartId(), node);
            if (node.isParallelGateway()) {
                List<GatewayExt> leftList = gatewayExts.subList(index, gatewayExts.size());
                this.findParallelGatewayEnd(leftList, ext.getPassCount(), node, fullGatewayMap, scannedSet);
            }
        }
        return fullGatewayMap;
    }

    private void findParallelGatewayEnd(
            List<GatewayExt> leftList,
            int passCount,
            AnalyzedNode node,
            Map<String, AnalyzedNode> fullGatewayMap,
            Set<String> scannedSet) {
        List<GatewayExt> subList = new LinkedList<>();
        for (GatewayExt ext2 : leftList) {
            if (ext2.getPassCount() == passCount) {
                node.setEndId(ext2.gateway.getId());
                scannedSet.add(ext2.gateway.getId());
                if (CollUtil.isNotEmpty(subList)) {
                    fullGatewayMap.putAll(this.analyzeGatewayPair(subList, scannedSet));
                    scannedSet.addAll(subList.stream()
                            .map(subExt -> subExt.getGateway().getId()).collect(Collectors.toList()));
                }
                break;
            } else {
                subList.add(ext2);
            }
        }
    }

    private List<GatewayExt> calculateGatewayPassCount(List<List<FlowElement>> roads) {
        List<GatewayExt> gatewayExts = new LinkedList<>();
        for (List<FlowElement> road : roads) {
            road.stream().filter(Gateway.class::isInstance).forEach(el -> {
                GatewayExt gatewayExt = gatewayExts.stream()
                        .filter(ext -> ext.gateway.getId().equals(el.getId())).findFirst().orElse(null);
                if (gatewayExt != null) {
                    gatewayExt.setPassCount(gatewayExt.getPassCount() + 1);
                } else {
                    gatewayExts.add(new GatewayExt((Gateway) el));
                }
            });
        }
        return gatewayExts;
    }

    @Data
    @AllArgsConstructor
    static class GatewayExt {
        private Gateway gateway;
        private int passCount = 1;

        public GatewayExt(Gateway gateway) {
            this.gateway = gateway;
        }
    }

    private List<List<FlowElement>> findForwardRoad(
            FlowElement source,
            List<FlowElement> passRoads,
            Set<String> hasSequenceFlow,
            List<List<FlowElement>> roads) {
        List<SequenceFlow> sequenceFlows = getElementOutgoingFlows(source);
        if (CollUtil.isEmpty(sequenceFlows)) {
            roads.add(passRoads);
            return roads;
        }
        for (SequenceFlow sequenceFlow : sequenceFlows) {
            // 如果发现连线重复，说明循环了，跳过这个循环
            if (hasSequenceFlow.contains(sequenceFlow.getId())) {
                continue;
            }
            hasSequenceFlow.add(sequenceFlow.getId());
            FlowElement element = sequenceFlow.getTargetFlowElement();
            boolean pushed = false;
            if (element instanceof UserTask
                    || element instanceof ParallelGateway
                    || element instanceof ExclusiveGateway) {
                passRoads.add(element);
                pushed = true;
            }
            roads = findForwardRoad(sequenceFlow.getTargetFlowElement(),
                    new ArrayList<>(passRoads), new HashSet<>(hasSequenceFlow), roads);
            if (pushed) {
                passRoads.remove(element);
            }
        }
        return roads;
    }

    private List<List<UserTask>> findRoad(
            FlowElement source, List<UserTask> passRoads, Set<String> hasSequenceFlow, List<List<UserTask>> roads) {
        passRoads = passRoads == null ? new ArrayList<>() : passRoads;
        roads = roads == null ? new ArrayList<>() : roads;
        hasSequenceFlow = hasSequenceFlow == null ? new HashSet<>() : hasSequenceFlow;
        if (source instanceof StartEvent && source.getSubProcess() != null) {
            roads = findRoad(source.getSubProcess(), passRoads, hasSequenceFlow, roads);
        }
        List<SequenceFlow> sequenceFlows = getElementIncomingFlows(source);
        if (CollUtil.isEmpty(sequenceFlows)) {
            roads.add(passRoads);
            return roads;
        }
        for (SequenceFlow sequenceFlow : sequenceFlows) {
            // 如果发现连线重复，说明循环了，跳过这个循环
            if (hasSequenceFlow.contains(sequenceFlow.getId())) {
                continue;
            }
            hasSequenceFlow.add(sequenceFlow.getId());
            UserTask nowUserTask = null;
            if (sequenceFlow.getSourceFlowElement() instanceof UserTask
                    && !((UserTask) sequenceFlow.getSourceFlowElement()).hasMultiInstanceLoopCharacteristics()) {
                nowUserTask = (UserTask) sequenceFlow.getSourceFlowElement();
                passRoads.add(nowUserTask);
            }
            roads = findRoad(sequenceFlow.getSourceFlowElement(),
                    new ArrayList<>(passRoads), new HashSet<>(hasSequenceFlow), roads);
            if (nowUserTask != null) {
                passRoads.remove(nowUserTask);
            }
        }
        return roads;
    }

    private List<UserTask> getParentUserTaskList(
            FlowElement source, Set<String> hasSequenceFlow, List<UserTask> userTaskList) {
        // 如果该节点为开始节点，且存在上级子节点，则顺着上级子节点继续迭代
        if (source instanceof StartEvent && source.getSubProcess() != null) {
            userTaskList = getParentUserTaskList(source.getSubProcess(), hasSequenceFlow, userTaskList);
        }
        List<SequenceFlow> sequenceFlows = getElementIncomingFlows(source);
        if (sequenceFlows != null) {
            // 循环找到目标元素
            for (SequenceFlow sequenceFlow : sequenceFlows) {
                // 如果发现连线重复，说明循环了，跳过这个循环
                if (!hasSequenceFlow.contains(sequenceFlow.getId())) {
                    // 添加已经走过的连线
                    hasSequenceFlow.add(sequenceFlow.getId());
                    this.findParentUserTaskListByFlow(sequenceFlow, hasSequenceFlow, userTaskList);
                }
            }
        }
        return userTaskList;
    }

    private void findParentUserTaskListByFlow(
            SequenceFlow sequenceFlow, Set<String> hasSequenceFlow, List<UserTask> userTaskList) {
        // 类型为用户节点，则新增父级节点
        if (sequenceFlow.getSourceFlowElement() instanceof UserTask) {
            userTaskList.add((UserTask) sequenceFlow.getSourceFlowElement());
            return;
        }
        getParentUserTaskList(sequenceFlow.getSourceFlowElement(), new HashSet<>(hasSequenceFlow), userTaskList);
    }

    private List<UserTask> getChildUserTaskList(
            FlowElement source, Set<String> hasSequenceFlow, List<UserTask> userTaskList) {
        // 根据类型，获取出口连线
        List<SequenceFlow> sequenceFlows = getElementOutgoingFlows(source);
        if (sequenceFlows != null) {
            // 循环找到目标元素
            for (SequenceFlow sequenceFlow : sequenceFlows) {
                // 如果发现连线重复，说明循环了，跳过这个循环
                if (!hasSequenceFlow.contains(sequenceFlow.getId())) {
                    // 添加已经走过的连线
                    hasSequenceFlow.add(sequenceFlow.getId());
                    this.findChildUserTaskListByFlow(sequenceFlow, hasSequenceFlow, userTaskList);
                }
            }
        }
        return userTaskList;
    }

    private void findChildUserTaskListByFlow(
            SequenceFlow sequenceFlow, Set<String> hasSequenceFlow, List<UserTask> userTaskList) {
        FlowElement targetElement = sequenceFlow.getTargetFlowElement();
        if (targetElement instanceof UserTask) {
            userTaskList.add((UserTask) sequenceFlow.getTargetFlowElement());
            return;
        }
        getChildUserTaskList(sequenceFlow.getTargetFlowElement(), new HashSet<>(hasSequenceFlow), userTaskList);
    }

    private void handleMultiInstanceApprovalType(String executionId, String approvalType, JSONObject taskVariableData) {
        if (StrUtil.isBlank(approvalType)) {
            return;
        }
        if (StrUtil.equalsAny(approvalType,
                FlowApprovalType.MULTI_AGREE,
                FlowApprovalType.MULTI_REFUSE,
                FlowApprovalType.MULTI_ABSTAIN)) {
            Map<String, Object> variables = runtimeService.getVariables(executionId);
            Integer agreeCount = (Integer) variables.get(FlowConstant.MULTI_AGREE_COUNT_VAR);
            Integer refuseCount = (Integer) variables.get(FlowConstant.MULTI_REFUSE_COUNT_VAR);
            Integer abstainCount = (Integer) variables.get(FlowConstant.MULTI_ABSTAIN_COUNT_VAR);
            Integer nrOfInstances = (Integer) variables.get(FlowConstant.NUMBER_OF_INSTANCES_VAR);
            taskVariableData.put(FlowConstant.MULTI_AGREE_COUNT_VAR, agreeCount);
            taskVariableData.put(FlowConstant.MULTI_REFUSE_COUNT_VAR, refuseCount);
            taskVariableData.put(FlowConstant.MULTI_ABSTAIN_COUNT_VAR, abstainCount);
            taskVariableData.put(FlowConstant.MULTI_SIGN_NUM_OF_INSTANCES_VAR, nrOfInstances);
            switch (approvalType) {
                case FlowApprovalType.MULTI_AGREE:
                    if (agreeCount == null) {
                        agreeCount = 0;
                    }
                    taskVariableData.put(FlowConstant.MULTI_AGREE_COUNT_VAR, agreeCount + 1);
                    break;
                case FlowApprovalType.MULTI_REFUSE:
                    if (refuseCount == null) {
                        refuseCount = 0;
                    }
                    taskVariableData.put(FlowConstant.MULTI_REFUSE_COUNT_VAR, refuseCount + 1);
                    break;
                case FlowApprovalType.MULTI_ABSTAIN:
                    if (abstainCount == null) {
                        abstainCount = 0;
                    }
                    taskVariableData.put(FlowConstant.MULTI_ABSTAIN_COUNT_VAR, abstainCount + 1);
                    break;
                default:
                    break;
            }
        }
    }

    private TaskQuery createQuery() {
        TaskQuery query = taskService.createTaskQuery().active();
        TokenData tokenData = TokenData.takeFromRequest();
        if (tokenData.getTenantId() != null) {
            query.taskTenantId(tokenData.getTenantId().toString());
        } else {
            if (StrUtil.isBlank(tokenData.getAppCode())) {
                query.taskWithoutTenantId();
            } else {
                query.taskTenantId(tokenData.getAppCode());
            }
        }
        return query;
    }
    private void buildCandidateCondition(TaskQuery query, String loginName) {
        Set<String> groupIdSet = new HashSet<>();
        // NOTE: 需要注意的是，部门Id、部门岗位Id，或者其他类型的分组Id，他们之间一定不能重复。
        TokenData tokenData = TokenData.takeFromRequest();
        Object deptId = tokenData.getDeptId();
        if (deptId != null) {
            groupIdSet.add(deptId.toString());
        }
        String roleIds = tokenData.getRoleIds();
        if (StrUtil.isNotBlank(tokenData.getRoleIds())) {
            groupIdSet.addAll(StrUtil.split(roleIds, ","));
        }
        String postIds = tokenData.getPostIds();
        if (StrUtil.isNotBlank(tokenData.getPostIds())) {
            groupIdSet.addAll(StrUtil.split(postIds, ","));
        }
        String deptPostIds = tokenData.getDeptPostIds();
        if (StrUtil.isNotBlank(deptPostIds)) {
            groupIdSet.addAll(StrUtil.split(deptPostIds, ","));
        }
        if (CollUtil.isNotEmpty(groupIdSet)) {
            query.or().taskCandidateGroupIn(groupIdSet).taskCandidateOrAssigned(loginName).endOr();
        } else {
            query.taskCandidateOrAssigned(loginName);
        }
    }

    private String buildMutiSignAssigneeList(String operationListJson) {
        FlowTaskMultiSignAssign multiSignAssignee = null;
        List<FlowTaskOperation> taskOperationList = JSONArray.parseArray(operationListJson, FlowTaskOperation.class);
        for (FlowTaskOperation taskOperation : taskOperationList) {
            if (FlowApprovalType.MULTI_SIGN.equals(taskOperation.getType())) {
                multiSignAssignee = taskOperation.getMultiSignAssignee();
                break;
            }
        }
        org.springframework.util.Assert.notNull(multiSignAssignee, "multiSignAssignee can't be NULL");
        if (UserFilterGroup.USER.equals(multiSignAssignee.getAssigneeType())) {
            return multiSignAssignee.getAssigneeList();
        }
        Set<String> usernameSet = null;
        BaseFlowIdentityExtHelper extHelper = flowCustomExtFactory.getFlowIdentityExtHelper();
        Set<String> idSet = CollUtil.newHashSet(StrUtil.split(multiSignAssignee.getAssigneeList(), ","));
        switch (multiSignAssignee.getAssigneeType()) {
            case UserFilterGroup.ROLE:
                usernameSet = extHelper.getUsernameListByRoleIds(idSet);
                break;
            case UserFilterGroup.DEPT:
                usernameSet = extHelper.getUsernameListByDeptIds(idSet);
                break;
            case UserFilterGroup.POST:
                usernameSet = extHelper.getUsernameListByPostIds(idSet);
                break;
            case UserFilterGroup.DEPT_POST:
                usernameSet = extHelper.getUsernameListByDeptPostIds(idSet);
                break;
            default:
                break;
        }
        return CollUtil.isEmpty(usernameSet) ? null : CollUtil.join(usernameSet, ",");
    }

    private Collection<FlowElement> getAllElements(Collection<FlowElement> flowElements, Collection<FlowElement> allElements) {
        allElements = allElements == null ? new ArrayList<>() : allElements;
        for (FlowElement flowElement : flowElements) {
            allElements.add(flowElement);
            if (flowElement instanceof SubProcess) {
                allElements = getAllElements(((SubProcess) flowElement).getFlowElements(), allElements);
            }
        }
        return allElements;
    }

    private void doChangeTask(Task runtimeTask) {
        Map<String, UserTask> allUserTaskMap =
                this.getAllUserTaskMap(runtimeTask.getProcessDefinitionId());
        UserTask userTaskModel = allUserTaskMap.get(runtimeTask.getTaskDefinitionKey());
        String completeCondition = userTaskModel.getLoopCharacteristics().getCompletionCondition();
        Execution parentExecution = this.getMultiInstanceRootExecution(runtimeTask);
        Object nrOfCompletedInstances = runtimeService.getVariable(
                parentExecution.getId(), FlowConstant.NUMBER_OF_COMPLETED_INSTANCES_VAR);
        Object nrOfInstances = runtimeService.getVariable(
                parentExecution.getId(), FlowConstant.NUMBER_OF_INSTANCES_VAR);
        ExpressionFactory factory = new ExpressionFactoryImpl();
        SimpleContext context = new SimpleContext();
        context.setVariable("nrOfCompletedInstances",
                factory.createValueExpression(nrOfCompletedInstances, Integer.class));
        context.setVariable("nrOfInstances",
                factory.createValueExpression(nrOfInstances, Integer.class));
        ValueExpression e = factory.createValueExpression(context, completeCondition, Boolean.class);
        Boolean ok = Convert.convert(Boolean.class, e.getValue(context));
        if (BooleanUtil.isTrue(ok)) {
            FlowElement targetKey = userTaskModel.getOutgoingFlows().get(0).getTargetFlowElement();
            ChangeActivityStateBuilder builder = runtimeService.createChangeActivityStateBuilder()
                    .processInstanceId(runtimeTask.getProcessInstanceId())
                    .moveActivityIdTo(userTaskModel.getId(), targetKey.getId());
            builder.localVariable(targetKey.getId(), FlowConstant.MULTI_SIGN_NUM_OF_INSTANCES_VAR, nrOfInstances);
            builder.changeState();
        }
    }


    private Execution getMultiInstanceRootExecution(Task runtimeTask) {
        List<Execution> executionList = runtimeService.createExecutionQuery()
                .processInstanceId(runtimeTask.getProcessInstanceId())
                .activityId(runtimeTask.getTaskDefinitionKey()).list();
        for (Execution e : executionList) {
            ExecutionEntityImpl ee = (ExecutionEntityImpl) e;
            if (ee.isMultiInstanceRoot()) {
                return e;
            }
        }
        Execution execution = executionList.get(0);
        return runtimeService.createExecutionQuery()
                .processInstanceId(runtimeTask.getProcessInstanceId())
                .executionId(execution.getParentId()).singleResult();
    }

    private List<SequenceFlow> getElementIncomingFlows(FlowElement source) {
        List<SequenceFlow> sequenceFlows = null;
        if (source instanceof org.flowable.bpmn.model.Task) {
            sequenceFlows = ((org.flowable.bpmn.model.Task) source).getIncomingFlows();
        } else if (source instanceof Gateway) {
            sequenceFlows = ((Gateway) source).getIncomingFlows();
        } else if (source instanceof SubProcess) {
            sequenceFlows = ((SubProcess) source).getIncomingFlows();
        } else if (source instanceof StartEvent) {
            sequenceFlows = ((StartEvent) source).getIncomingFlows();
        } else if (source instanceof EndEvent) {
            sequenceFlows = ((EndEvent) source).getIncomingFlows();
        }
        return sequenceFlows;
    }

    private List<SequenceFlow> getElementOutgoingFlows(FlowElement source) {
        List<SequenceFlow> sequenceFlows = null;
        if (source instanceof org.flowable.bpmn.model.Task) {
            sequenceFlows = ((org.flowable.bpmn.model.Task) source).getOutgoingFlows();
        } else if (source instanceof Gateway) {
            sequenceFlows = ((Gateway) source).getOutgoingFlows();
        } else if (source instanceof SubProcess) {
            sequenceFlows = ((SubProcess) source).getOutgoingFlows();
        } else if (source instanceof StartEvent) {
            sequenceFlows = ((StartEvent) source).getOutgoingFlows();
        } else if (source instanceof EndEvent) {
            sequenceFlows = ((EndEvent) source).getOutgoingFlows();
        }
        return sequenceFlows;
    }

    private FlowableListener createListener(String eventName, String listenerClassName) {
        FlowableListener listener = new FlowableListener();
        listener.setEvent(eventName);
        listener.setImplementationType("class");
        listener.setImplementation(listenerClassName);
        return listener;
    }

    private FlowElement findFlement(Collection<FlowElement> allElements, String taskKey) {
        for (FlowElement flowElement : allElements) {
            if (flowElement.getId().equals(taskKey)) {
                return flowElement;
            }
        }
        return null;
    }

    private UserTask getBackParentUserTask(List<UserTask> parentUserTaskList, Task task) {
        List<FlowTaskComment> comments = flowTaskCommentService.getFlowTaskCommentList(task.getProcessInstanceId());
        if (CollUtil.isEmpty(comments)) {
            return null;
        }
        List<FlowTaskComment> reversedComments = CollUtil.reverse(comments);
        for (FlowTaskComment comment : reversedComments) {
            for (UserTask parentTask : parentUserTaskList) {
                if (parentTask.getId().equals(comment.getTaskKey())) {
                    return parentTask;
                }
            }
        }
        return null;
    }
}
