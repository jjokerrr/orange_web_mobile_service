package com.bupt.common.flow.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bupt.common.core.annotation.MyDataSourceResolver;
import com.bupt.common.core.base.dao.BaseDaoMapper;
import com.bupt.common.core.base.service.BaseService;
import com.bupt.common.core.constant.ApplicationConstant;
import com.bupt.common.flow.constant.FlowConstant;
import com.bupt.common.core.object.TokenData;
import com.bupt.common.core.util.DefaultDataSourceResolver;
import com.bupt.common.flow.model.*;
import com.bupt.common.flow.model.constant.FlowMessageOperationType;
import com.bupt.common.flow.model.constant.FlowMessageType;
import com.bupt.common.flow.dao.FlowMessageIdentityOperationMapper;
import com.bupt.common.flow.dao.FlowMessageCandidateIdentityMapper;
import com.bupt.common.flow.dao.FlowMessageMapper;
import com.bupt.common.flow.object.FlowTaskPostCandidateGroup;
import com.bupt.common.flow.service.FlowApiService;
import com.bupt.common.flow.service.FlowMessageService;
import com.bupt.common.flow.service.FlowTaskExtService;
import com.bupt.common.flow.util.FlowCustomExtFactory;
import com.bupt.common.flow.util.BaseBusinessDataExtHelper;
import com.bupt.common.flow.vo.TaskInfoVo;
import com.bupt.common.sequence.wrapper.IdGeneratorWrapper;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 工作流消息数据操作服务接口。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Slf4j
@MyDataSourceResolver(
        resolver = DefaultDataSourceResolver.class,
        intArg = ApplicationConstant.COMMON_FLOW_AND_ONLINE_DATASOURCE_TYPE)
@Service("flowMessageService")
public class FlowMessageServiceImpl extends BaseService<FlowMessage, Long> implements FlowMessageService {

    @Autowired
    private FlowMessageMapper flowMessageMapper;
    @Autowired
    private FlowMessageCandidateIdentityMapper flowMessageCandidateIdentityMapper;
    @Autowired
    private FlowMessageIdentityOperationMapper flowMessageIdentityOperationMapper;
    @Autowired
    private FlowTaskExtService flowTaskExtService;
    @Autowired
    private FlowApiService flowApiService;
    @Autowired
    private FlowCustomExtFactory flowCustomExtFactory;
    @Autowired
    private IdGeneratorWrapper idGenerator;

    /**
     * 返回当前Service的主表Mapper对象。
     *
     * @return 主表Mapper对象。
     */
    @Override
    protected BaseDaoMapper<FlowMessage> mapper() {
        return flowMessageMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public FlowMessage saveNew(FlowMessage flowMessage) {
        flowMessage.setMessageId(idGenerator.nextLongId());
        TokenData tokenData = TokenData.takeFromRequest();
        flowMessage.setTenantId(tokenData.getTenantId());
        flowMessage.setAppCode(tokenData.getAppCode());
        flowMessage.setCreateUserId(tokenData.getUserId());
        flowMessage.setCreateUsername(tokenData.getShowName());
        flowMessage.setCreateTime(new Date());
        flowMessage.setUpdateUserId(tokenData.getUserId());
        flowMessage.setUpdateTime(flowMessage.getCreateTime());
        flowMessageMapper.insert(flowMessage);
        return flowMessage;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveNewRemindMessage(FlowWorkOrder flowWorkOrder) {
        List<Task> taskList =
                flowApiService.getProcessInstanceActiveTaskList(flowWorkOrder.getProcessInstanceId());
        for (Task task : taskList) {
            FlowMessage filter = new FlowMessage();
            filter.setTaskId(task.getId());
            List<FlowMessage> messageList = flowMessageMapper.selectList(new QueryWrapper<>(filter));
            // 同一个任务只能催办一次，多次催办则累加催办次数。
            if (CollUtil.isNotEmpty(messageList)) {
                for (FlowMessage flowMessage : messageList) {
                    flowMessage.setRemindCount(flowMessage.getRemindCount() + 1);
                    flowMessageMapper.updateById(flowMessage);
                }
                continue;
            }
            FlowMessage flowMessage = new FlowMessage();
            flowMessage.setMessageType(FlowMessageType.REMIND_TYPE);
            flowMessage.setRemindCount(1);
            flowMessage.setWorkOrderId(flowWorkOrder.getWorkOrderId());
            flowMessage.setProcessDefinitionId(flowWorkOrder.getProcessDefinitionId());
            flowMessage.setProcessDefinitionKey(flowWorkOrder.getProcessDefinitionKey());
            flowMessage.setProcessDefinitionName(flowWorkOrder.getProcessDefinitionName());
            flowMessage.setProcessInstanceId(flowWorkOrder.getProcessInstanceId());
            flowMessage.setProcessInstanceInitiator(flowWorkOrder.getSubmitUsername());
            flowMessage.setTaskId(task.getId());
            flowMessage.setTaskDefinitionKey(task.getTaskDefinitionKey());
            flowMessage.setTaskName(task.getName());
            flowMessage.setTaskStartTime(task.getCreateTime());
            flowMessage.setTaskAssignee(task.getAssignee());
            flowMessage.setTaskFinished(false);
            this.saveNew(flowMessage);
            FlowTaskExt flowTaskExt = flowTaskExtService.getByProcessDefinitionIdAndTaskId(
                    flowWorkOrder.getProcessDefinitionId(), task.getTaskDefinitionKey());
            if (flowTaskExt != null) {
                // 插入与当前消息关联任务的候选人
                this.saveMessageCandidateIdentityWithMessage(
                        flowWorkOrder.getProcessInstanceId(), flowTaskExt, task, flowMessage.getMessageId());
            }
            // 插入与当前消息关联任务的指派人。
            if (StrUtil.isNotBlank(task.getAssignee())) {
                this.saveMessageCandidateIdentity(
                        flowMessage.getMessageId(), FlowConstant.GROUP_TYPE_USER_VAR, task.getAssignee());
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveNewCopyMessage(Task task, JSONObject copyDataJson) {
        if (copyDataJson.size() == 0) {
            return;
        }
        ProcessInstance instance = flowApiService.getProcessInstance(task.getProcessInstanceId());
        BaseBusinessDataExtHelper helper = flowCustomExtFactory.getBusinessDataExtHelper();
        // 在线表单中，这个值为空。
        String businessShotData = helper.getBusinessData(
                instance.getProcessDefinitionKey(), instance.getProcessInstanceId(), instance.getBusinessKey());
        FlowMessage flowMessage = new FlowMessage();
        flowMessage.setMessageType(FlowMessageType.COPY_TYPE);
        flowMessage.setRemindCount(0);
        flowMessage.setProcessDefinitionId(instance.getProcessDefinitionId());
        flowMessage.setProcessDefinitionKey(instance.getProcessDefinitionKey());
        flowMessage.setProcessDefinitionName(instance.getProcessDefinitionName());
        flowMessage.setProcessInstanceId(instance.getProcessInstanceId());
        flowMessage.setProcessInstanceInitiator(instance.getStartUserId());
        flowMessage.setTaskId(task.getId());
        flowMessage.setTaskDefinitionKey(task.getTaskDefinitionKey());
        flowMessage.setTaskName(task.getName());
        flowMessage.setTaskStartTime(task.getCreateTime());
        flowMessage.setTaskAssignee(task.getAssignee());
        flowMessage.setTaskFinished(false);
        flowMessage.setBusinessDataShot(businessShotData);
        flowMessage.setOnlineFormData(businessShotData == null);
        // 如果是在线表单，这里就保存关联的在线表单Id，便于在线表单业务数据的查找。
        if (BooleanUtil.isTrue(flowMessage.getOnlineFormData())) {
            TaskInfoVo taskInfo = JSON.parseObject(task.getFormKey(), TaskInfoVo.class);
            flowMessage.setBusinessDataShot(taskInfo.getFormId().toString());
        }
        this.saveNew(flowMessage);
        for (Map.Entry<String, Object> entries : copyDataJson.entrySet()) {
            this.saveMessageCandidateIdentityList(
                    flowMessage.getMessageId(), entries.getKey(), entries.getValue().toString());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateFinishedStatusByTaskId(String taskId) {
        FlowMessage flowMessage = new FlowMessage();
        flowMessage.setTaskFinished(true);
        LambdaQueryWrapper<FlowMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FlowMessage::getTaskId, taskId);
        flowMessageMapper.update(flowMessage, queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateFinishedStatusByProcessInstanceId(String processInstanceId) {
        FlowMessage flowMessage = new FlowMessage();
        flowMessage.setTaskFinished(true);
        LambdaQueryWrapper<FlowMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FlowMessage::getProcessInstanceId, processInstanceId);
        flowMessageMapper.update(flowMessage, queryWrapper);
    }

    @Override
    public List<FlowMessage> getRemindingMessageListByUser() {
        TokenData tokenData = TokenData.takeFromRequest();
        return flowMessageMapper.getRemindingMessageListByUser(
                tokenData.getTenantId(), tokenData.getAppCode(), tokenData.getLoginName(), buildGroupIdSet());
    }

    @Override
    public List<FlowMessage> getCopyMessageListByUser(Boolean read) {
        TokenData tokenData = TokenData.takeFromRequest();
        return flowMessageMapper.getCopyMessageListByUser(
                tokenData.getTenantId(), tokenData.getAppCode(), tokenData.getLoginName(), buildGroupIdSet(), read);
    }

    @Override
    public boolean isCandidateIdentityOnMessage(Long messageId) {
        LambdaQueryWrapper<FlowMessageCandidateIdentity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FlowMessageCandidateIdentity::getMessageId, messageId);
        queryWrapper.in(FlowMessageCandidateIdentity::getCandidateId, buildGroupIdSet());
        return flowMessageCandidateIdentityMapper.selectCount(queryWrapper) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void readCopyTask(Long messageId) {
        FlowMessageIdentityOperation operation = new FlowMessageIdentityOperation();
        operation.setId(idGenerator.nextLongId());
        operation.setMessageId(messageId);
        operation.setLoginName(TokenData.takeFromRequest().getLoginName());
        operation.setOperationType(FlowMessageOperationType.READ_FINISHED);
        operation.setOperationTime(new Date());
        flowMessageIdentityOperationMapper.insert(operation);
    }

    @Override
    public int countRemindingMessageListByUser() {
        TokenData tokenData = TokenData.takeFromRequest();
        return flowMessageMapper.countRemindingMessageListByUser(
                tokenData.getTenantId(), tokenData.getAppCode(), tokenData.getLoginName(), buildGroupIdSet());
    }

    @Override
    public int countCopyMessageByUser() {
        TokenData tokenData = TokenData.takeFromRequest();
        return flowMessageMapper.countCopyMessageListByUser(
                tokenData.getTenantId(), tokenData.getAppCode(), tokenData.getLoginName(), buildGroupIdSet());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeByProcessInstanceId(String processInstanceId) {
        flowMessageCandidateIdentityMapper.deleteByProcessInstanceId(processInstanceId);
        flowMessageIdentityOperationMapper.deleteByProcessInstanceId(processInstanceId);
        LambdaQueryWrapper<FlowMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FlowMessage::getProcessInstanceId, processInstanceId);
        flowMessageMapper.delete(queryWrapper);
    }

    private Set<String> buildGroupIdSet() {
        TokenData tokenData = TokenData.takeFromRequest();
        Set<String> groupIdSet = new HashSet<>(1);
        groupIdSet.add(tokenData.getLoginName());
        this.parseAndAddIdArray(groupIdSet, tokenData.getRoleIds());
        this.parseAndAddIdArray(groupIdSet, tokenData.getDeptPostIds());
        this.parseAndAddIdArray(groupIdSet, tokenData.getPostIds());
        if (tokenData.getDeptId() != null) {
            groupIdSet.add(tokenData.getDeptId().toString());
        }
        return groupIdSet;
    }

    private void parseAndAddIdArray(Set<String> groupIdSet, String idArray) {
        if (StrUtil.isNotBlank(idArray)) {
            if (groupIdSet == null) {
                groupIdSet = new HashSet<>();
            }
            groupIdSet.addAll(StrUtil.split(idArray, ','));
        }
    }

    private void saveMessageCandidateIdentityWithMessage(
            String processInstanceId, FlowTaskExt flowTaskExt, Task task, Long messageId) {
        List<String> candidates = flowApiService.getCandidateUsernames(flowTaskExt, task.getId());
        if (CollUtil.isNotEmpty(candidates)) {
            this.saveMessageCandidateIdentityList(
                    messageId, FlowConstant.GROUP_TYPE_USER_VAR, CollUtil.join(candidates, ","));
        }
        this.saveMessageCandidateIdentityList(
                messageId, FlowConstant.GROUP_TYPE_ROLE_VAR, flowTaskExt.getRoleIds());
        this.saveMessageCandidateIdentityList(
                messageId, FlowConstant.GROUP_TYPE_DEPT_VAR, flowTaskExt.getDeptIds());
        if (StrUtil.equals(flowTaskExt.getGroupType(), FlowConstant.GROUP_TYPE_UP_DEPT_POST_LEADER)) {
            Object v = flowApiService.getProcessInstanceVariable(
                    processInstanceId, FlowConstant.GROUP_TYPE_UP_DEPT_POST_LEADER_VAR);
            if (v != null) {
                this.saveMessageCandidateIdentity(
                        messageId, FlowConstant.GROUP_TYPE_UP_DEPT_POST_LEADER_VAR, v.toString());
            }
        } else if (StrUtil.equals(flowTaskExt.getGroupType(), FlowConstant.GROUP_TYPE_DEPT_POST_LEADER)) {
            Object v = flowApiService.getProcessInstanceVariable(
                    processInstanceId, FlowConstant.GROUP_TYPE_DEPT_POST_LEADER_VAR);
            if (v != null) {
                this.saveMessageCandidateIdentity(
                        messageId, FlowConstant.GROUP_TYPE_DEPT_POST_LEADER_VAR, v.toString());
            }
        } else if (StrUtil.equals(flowTaskExt.getGroupType(), FlowConstant.GROUP_TYPE_POST)) {
            Assert.notBlank(flowTaskExt.getDeptPostListJson());
            List<FlowTaskPostCandidateGroup> groupDataList =
                    JSONArray.parseArray(flowTaskExt.getDeptPostListJson(), FlowTaskPostCandidateGroup.class);
            for (FlowTaskPostCandidateGroup groupData : groupDataList) {
                this.saveMessageCandidateIdentity(messageId, processInstanceId, groupData);
            }
        }
    }

    private void saveMessageCandidateIdentity(
            Long messageId, String processInstanceId, FlowTaskPostCandidateGroup groupData) {
        FlowMessageCandidateIdentity candidateIdentity = new FlowMessageCandidateIdentity();
        candidateIdentity.setId(idGenerator.nextLongId());
        candidateIdentity.setMessageId(messageId);
        candidateIdentity.setCandidateType(groupData.getType());
        switch (groupData.getType()) {
            case FlowConstant.GROUP_TYPE_ALL_DEPT_POST_VAR:
                candidateIdentity.setCandidateId(groupData.getPostId());
                flowMessageCandidateIdentityMapper.insert(candidateIdentity);
                break;
            case FlowConstant.GROUP_TYPE_DEPT_POST_VAR:
                candidateIdentity.setCandidateId(groupData.getDeptPostId());
                flowMessageCandidateIdentityMapper.insert(candidateIdentity);
                break;
            case FlowConstant.GROUP_TYPE_SELF_DEPT_POST_VAR:
                Object v = flowApiService.getProcessInstanceVariable(
                        processInstanceId, FlowConstant.SELF_DEPT_POST_PREFIX + groupData.getPostId());
                if (v != null) {
                    candidateIdentity.setCandidateId(v.toString());
                    flowMessageCandidateIdentityMapper.insert(candidateIdentity);
                }
                break;
            case FlowConstant.GROUP_TYPE_UP_DEPT_POST_VAR:
                Object v2 = flowApiService.getProcessInstanceVariable(
                        processInstanceId, FlowConstant.UP_DEPT_POST_PREFIX + groupData.getPostId());
                if (v2 != null) {
                    candidateIdentity.setCandidateId(v2.toString());
                    flowMessageCandidateIdentityMapper.insert(candidateIdentity);
                }
                break;
            case FlowConstant.GROUP_TYPE_SIBLING_DEPT_POST_VAR:
                Object v3 = flowApiService.getProcessInstanceVariable(
                        processInstanceId, FlowConstant.SIBLING_DEPT_POST_PREFIX + groupData.getPostId());
                if (v3 != null) {
                    List<String> candidateIds = StrUtil.split(v3.toString(), ",");
                    for (String candidateId : candidateIds) {
                        candidateIdentity.setId(idGenerator.nextLongId());
                        candidateIdentity.setCandidateId(candidateId);
                        flowMessageCandidateIdentityMapper.insert(candidateIdentity);
                    }
                }
                break;
            default:
                break;
        }
    }
    private void saveMessageCandidateIdentity(Long messageId, String candidateType, String candidateId) {
        FlowMessageCandidateIdentity candidateIdentity = new FlowMessageCandidateIdentity();
        candidateIdentity.setId(idGenerator.nextLongId());
        candidateIdentity.setMessageId(messageId);
        candidateIdentity.setCandidateType(candidateType);
        candidateIdentity.setCandidateId(candidateId);
        flowMessageCandidateIdentityMapper.insert(candidateIdentity);
    }

    private void saveMessageCandidateIdentityList(Long messageId, String candidateType, String identityIds) {
        if (StrUtil.isNotBlank(identityIds)) {
            for (String identityId : StrUtil.split(identityIds, ',')) {
                FlowMessageCandidateIdentity candidateIdentity = new FlowMessageCandidateIdentity();
                candidateIdentity.setId(idGenerator.nextLongId());
                candidateIdentity.setMessageId(messageId);
                candidateIdentity.setCandidateType(candidateType);
                candidateIdentity.setCandidateId(identityId);
                flowMessageCandidateIdentityMapper.insert(candidateIdentity);
            }
        }
    }
}
