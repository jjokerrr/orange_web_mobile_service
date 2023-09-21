package com.bupt.common.flow.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 流程处理事务事件生产者流水实体。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
@TableName(value = "zz_flow_trans_producer")
public class FlowTransProducer {

    /**
     * 流水Id。
     */
    @TableId(value = "trans_id")
    private Long transId;

    /**
     * 应用编码。为空时，表示非第三方应用接入。
     */
    @TableField(value = "app_code")
    private String appCode;

    /**
     * 业务数据库链接Id。
     */
    @TableField(value = "dblink_id")
    private Long dblinkId;

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
     * 审批批注。
     */
    @TableField(value = "task_comment")
    private String taskComment;

    /**
     * 当前请求的url。
     */
    @TableField(value = "url")
    private String url;

    /**
     * 创建该事务性事件对象的初始方法。格式为：方法名(参数类型1,参数类型2)。
     */
    @TableField(value = "init_method")
    private String initMethod;

    /**
     * 当前请求的traceId。
     */
    @TableField(value = "trace_id")
    private String traceId;

    /**
     * 和SQL操作相关的数据。值类型为TransactionalBusinessData.BusinessSqlData对象。
     */
    @TableField(value = "sql_data")
    private String sqlData;

    /**
     * 尝试次数。默认的插入值为1。
     */
    @TableField(value = "try_times")
    private Integer tryTimes;

    /**
     * 提交业务数据时的错误信息。如果正常提交，该值为空。
     */
    @TableField(value = "error_reason")
    private String errorReason;

    /**
     * 创建者登录名。
     */
    @TableField(value = "create_login_name")
    private String createLoginName;

    /**
     * 创建者中文用户名。
     */
    @TableField(value = "create_username")
    private String createUsername;

    /**
     * 创建时间。
     */
    @TableField(value = "create_time")
    private Date createTime;
}
