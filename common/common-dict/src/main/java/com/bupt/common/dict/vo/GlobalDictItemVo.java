package com.bupt.common.dict.vo;

import lombok.Data;

import java.util.Date;

/**
 * 全局系统字典项目Vo。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
public class GlobalDictItemVo {

    /**
     * 主键Id。
     */
    private Long id;

    /**
     * 字典编码。
     */
    private String dictCode;

    /**
     * 字典数据项Id。
     */
    private String itemId;

    /**
     * 字典数据项名称。
     */
    private String itemName;

    /**
     * 显示顺序(数值越小越靠前)。
     */
    private Integer showOrder;

    /**
     * 字典状态。具体值引用DictItemStatus常量类。
     */
    private Integer status;

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
