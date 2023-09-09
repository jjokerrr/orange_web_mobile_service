package com.bupt.common.online.vo;

import lombok.Data;

import java.util.Date;

/**
 * 在线表单的数据表VO对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
public class OnlineTableVo {

    /**
     * 主键Id。
     */
    private Long tableId;

    /**
     * 应用编码。为空时，表示非第三方应用接入。
     */
    private String appCode;

    /**
     * 表名称。
     */
    private String tableName;

    /**
     * 实体名称。
     */
    private String modelName;

    /**
     * 数据库链接Id。
     */
    private Long dblinkId;

    /**
     * 创建时间。
     */
    private Date createTime;

    /**
     * 创建者。
     */
    private Long createUserId;

    /**
     * 更新时间。
     */
    private Date updateTime;

    /**
     * 更新者。
     */
    private Long updateUserId;
}
