package com.bupt.common.flow.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

/**
 * 流程处理事务事件消费者流水实体。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
@AllArgsConstructor
@TableName(value = "zz_flow_trans_consumer")
public class FlowTransConsumer {

    /**
     * 流水Id。
     */
    @TableId(value = "trans_id")
    private Long transId;

    /**
     * 创建时间。
     */
    @TableField(value = "create_time")
    private Date createTime;
}
