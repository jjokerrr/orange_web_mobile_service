package com.bupt.common.flow.model;

import com.baomidou.mybatisplus.annotation.*;
import com.bupt.common.core.base.mapper.BaseModelMapper;
import com.bupt.common.core.util.ContextUtil;
import com.bupt.common.flow.vo.FlowTaskCommentVo;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.flowable.task.api.TaskInfo;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.Date;

/**
 * FlowTaskComment实体对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
@NoArgsConstructor
@TableName(value = "zz_flow_task_comment")
public class FlowTaskComment {

    /**
     * 主键Id。
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 流程实例Id。
     */
    @TableField(value = "process_instance_id")
    private String processInstanceId;

    /**
     * 任务Id。
     */
    @TableField(value = "task_id")
    private String taskId;

    /**
     * 任务标识。
     */
    @TableField(value = "task_key")
    private String taskKey;

    /**
     * 任务名称。
     */
    @TableField(value = "task_name")
    private String taskName;

    /**
     * 用于驳回和自由跳的目标任务标识。
     */
    @TableField(value = "target_task_key")
    private String targetTaskKey;

    /**
     * 任务的执行Id。
     */
    @TableField(value = "execution_id")
    private String executionId;

    /**
     * 会签任务的执行Id。
     */
    @TableField(value = "multi_instance_exec_id")
    private String multiInstanceExecId;

    /**
     * 审批类型。
     */
    @TableField(value = "approval_type")
    private String approvalType;

    /**
     * 批注内容。
     */
    @TableField(value = "task_comment")
    private String taskComment;

    /**
     * 委托指定人，比如加签、转办等。
     */
    @TableField(value = "delegate_assignee")
    private String delegateAssignee;

    /**
     * 自定义数据。开发者可自行扩展，推荐使用JSON格式数据。
     */
    @TableField(value = "custom_business_data")
    private String customBusinessData;

    /**
     * 审批人头像。
     */
    @TableField(value = "head_image_url")
    private String headImageUrl;

    /**
     * 创建者Id。
     */
    @TableField(value = "create_user_id")
    private Long createUserId;

    /**
     * 创建者登录名。
     */
    @TableField(value = "create_login_name")
    private String createLoginName;

    /**
     * 创建者显示名。
     */
    @TableField(value = "create_username")
    private String createUsername;

    /**
     * 创建时间。
     */
    @TableField(value = "create_time")
    private Date createTime;

    private static final String REQ_ATTRIBUTE_KEY = "flowTaskComment";

    public FlowTaskComment(TaskInfo task) {
        this.fillWith(task);
    }

    public static void setToRequest(FlowTaskComment comment) {
        ContextUtil.getHttpRequest().setAttribute(REQ_ATTRIBUTE_KEY, comment);
    }

    public static FlowTaskComment getFromRequest() {
        return (FlowTaskComment) ContextUtil.getHttpRequest().getAttribute(REQ_ATTRIBUTE_KEY);
    }

    public void fillWith(TaskInfo task) {
        this.taskId = task.getId();
        this.taskKey = task.getTaskDefinitionKey();
        this.taskName = task.getName();
        this.processInstanceId = task.getProcessInstanceId();
        this.executionId = task.getExecutionId();
    }

    @Mapper
    public interface FlowTaskCommentModelMapper extends BaseModelMapper<FlowTaskCommentVo, FlowTaskComment> {
    }
    public static final FlowTaskCommentModelMapper INSTANCE = Mappers.getMapper(FlowTaskCommentModelMapper.class);
}
