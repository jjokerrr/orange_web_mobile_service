package com.bupt.common.flow.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.bupt.common.core.object.TokenData;
import com.bupt.common.core.util.ApplicationContextHolder;
import com.bupt.common.flow.constant.FlowApprovalType;
import com.bupt.common.flow.constant.FlowConstant;
import com.bupt.common.flow.model.FlowTaskComment;
import com.bupt.common.flow.model.FlowTaskExt;
import com.bupt.common.flow.service.FlowApiService;
import com.bupt.common.flow.service.FlowTaskCommentService;
import com.bupt.common.flow.service.FlowTaskExtService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.ExtensionAttribute;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;

import java.util.*;

/**
 * 流程任务自动审批跳过的监听器。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Slf4j
public class AutoSkipListener implements ExecutionListener {

    private final transient FlowTaskCommentService flowTaskCommentService =
            ApplicationContextHolder.getBean(FlowTaskCommentService.class);
    private final transient FlowApiService flowApiService =
            ApplicationContextHolder.getBean(FlowApiService.class);
    private final transient FlowTaskExtService flowTaskExtService =
            ApplicationContextHolder.getBean(FlowTaskExtService.class);

    /**
     * 流程的发起者等于当前任务的Assignee。
     */
    private static final String EQ_START_USER = "0";
    /**
     * 上一步的提交者等于当前任务的Assignee。
     */
    private static final String EQ_PREV_SUBMIT_USER = "1";
    /**
     * 当前任务的Assignee之前提交过审核。
     */
    private static final String EQ_HISTORIC_SUBMIT_USER = "2";

    @Override
    public void notify(DelegateExecution execution) {
        UserTask userTask = (UserTask) execution.getCurrentFlowElement();
        List<ExtensionAttribute> attributes = userTask.getAttributes().get(FlowConstant.USER_TASK_AUTO_SKIP_KEY);
        Set<String> skipTypes = new HashSet<>(StrUtil.split(attributes.get(0).getValue(), ","));
        String assignedUser = this.getAssignedUser(execution);
        if (StrUtil.isBlank(assignedUser)) {
            return;
        }
        for (String skipType : skipTypes) {
            if (this.verifyAndHandle(execution, skipType, assignedUser)) {
                return;
            }
        }
    }

    private boolean verifyAndHandle(DelegateExecution execution, String skipType, String assignedUser) {
        FlowTaskComment comment = null;
        switch (skipType) {
            case EQ_START_USER:
                Object v = execution.getVariable(FlowConstant.PROC_INSTANCE_START_USER_NAME_VAR);
                if (ObjectUtil.equal(v, assignedUser)) {
                    comment = flowTaskCommentService.getFirstFlowTaskComment(execution.getProcessInstanceId());
                }
                break;
            case EQ_PREV_SUBMIT_USER:
                Object v2 = execution.getVariable(FlowConstant.SUBMIT_USER_VAR);
                if (ObjectUtil.equal(v2, assignedUser)) {
                    TokenData tokenData = TokenData.takeFromRequest();
                    comment = new FlowTaskComment();
                    comment.setCreateUserId(tokenData.getUserId());
                    comment.setCreateLoginName(tokenData.getLoginName());
                    comment.setCreateUsername(tokenData.getShowName());
                }
                break;
            case EQ_HISTORIC_SUBMIT_USER:
                List<FlowTaskComment> comments =
                        flowTaskCommentService.getFlowTaskCommentList(execution.getProcessInstanceId());
                List<FlowTaskComment> resultComments = new LinkedList<>();
                for (FlowTaskComment c : comments) {
                    if (StrUtil.equals(c.getCreateLoginName(), assignedUser)) {
                        resultComments.add(c);
                    }
                }
                if (CollUtil.isNotEmpty(resultComments)) {
                    comment = resultComments.get(0);
                }
                break;
            default:
                break;
        }
        UserTask userTask = (UserTask) execution.getCurrentFlowElement();
        if (comment != null) {
            execution.setVariable("_FLOWABLE_SKIP_EXPRESSION_ENABLED", true);
            userTask.setSkipExpression("${1==1}");
            comment.setTaskId(userTask.getId());
            comment.setTaskKey(userTask.getId());
            comment.setTaskName(userTask.getName());
            comment.setProcessInstanceId(execution.getProcessInstanceId());
            comment.setExecutionId(execution.getId());
            comment.setApprovalType(FlowApprovalType.AGREE);
            comment.setTaskComment(StrFormatter.format("自动跳过审批。审批人 [{}], 跳过原因 [{}]。",
                    userTask.getAssignee(), this.getMessageBySkipType(skipType)));
            flowTaskCommentService.saveNew(comment);
        }
        return comment != null;
    }

    private String getAssignedUser(DelegateExecution execution) {
        UserTask userTask = (UserTask) execution.getCurrentFlowElement();
        String assignedUser = userTask.getAssignee();
        if (StrUtil.isNotBlank(assignedUser)) {
            if (assignedUser.startsWith("${") && assignedUser.endsWith("}")) {
                String variableName = assignedUser.substring(2, assignedUser.length() - 1);
                assignedUser = flowApiService.getExecutionVariableStringWithSafe(execution.getId(), variableName);
            }
        } else {
            FlowTaskExt flowTaskExt = flowTaskExtService
                    .getByProcessDefinitionIdAndTaskId(execution.getProcessDefinitionId(), userTask.getId());
            List<String> candidateUsernames;
            if (StrUtil.isBlank(flowTaskExt.getCandidateUsernames())) {
                candidateUsernames = Collections.emptyList();
            } else if (!StrUtil.equals(flowTaskExt.getCandidateUsernames(), "${" + FlowConstant.TASK_APPOINTED_ASSIGNEE_VAR + "}")) {
                candidateUsernames = StrUtil.split(flowTaskExt.getCandidateUsernames(), ",");
            } else {
                String value = flowApiService
                        .getExecutionVariableStringWithSafe(execution.getId(), FlowConstant.TASK_APPOINTED_ASSIGNEE_VAR);
                candidateUsernames = value == null ? null : StrUtil.split(value, ",");
            }
            if (candidateUsernames != null && candidateUsernames.size() == 1) {
                assignedUser = candidateUsernames.get(0);
            }
        }
        return assignedUser;
    }

    private String getMessageBySkipType(String skipType) {
        switch (skipType) {
            case EQ_PREV_SUBMIT_USER:
                return "审批人与上一审批节点处理人相同";
            case EQ_START_USER:
                return "审批人为发起人";
            case EQ_HISTORIC_SUBMIT_USER:
                return "审批人审批过";
            default:
                return "";
        }
    }
}
