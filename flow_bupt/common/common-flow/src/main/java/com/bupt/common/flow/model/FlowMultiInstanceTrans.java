package com.bupt.common.flow.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.flowable.task.api.TaskInfo;

import java.util.Date;

/**
 * 流程多实例任务执行流水对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
@NoArgsConstructor
@TableName(value = "zz_flow_multi_instance_trans")
public class FlowMultiInstanceTrans {

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
     * 会签任务的执行Id。
     */
    @TableField(value = "multi_instance_exec_id")
    private String multiInstanceExecId;

    /**
     * 任务的执行Id。
     */
    @TableField(value = "execution_id")
    private String executionId;

    /**
     * 会签指派人列表。
     */
    @TableField(value = "assignee_list")
    private String assigneeList;

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

    public FlowMultiInstanceTrans(TaskInfo task) {
        this.fillWith(task);
    }

    public void fillWith(TaskInfo task) {
        this.taskId = task.getId();
        this.taskKey = task.getTaskDefinitionKey();
        this.processInstanceId = task.getProcessInstanceId();
        this.executionId = task.getExecutionId();
    }
}
