package com.bupt.common.dict.vo;

import lombok.Data;

import java.util.Date;

/**
 * 全局系统字典Vo。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
public class GlobalDictVo {

    /**
     * 主键Id。
     */
    private Long dictId;

    /**
     * 字典编码。
     */
    private String dictCode;

    /**
     * 字典中文名称。
     */
    private String dictName;

    /**
     * 创建用户Id。
     */
    private Long createUserId;

    /**
     * 创建时间。
     */
    private Date createTime;

    /**
     * 创建用户名。
     */
    private Long updateUserId;

    /**
     * 更新时间。
     */
    private Date updateTime;
}
