package com.bupt.webadmin.upms.vo;

import lombok.Data;

import java.util.Date;

/**
 * SysDeptVO视图对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
public class SysDeptVo {

    /**
     * 部门Id。
     */
    private Long deptId;

    /**
     * 部门名称。
     */
    private String deptName;

    /**
     * 显示顺序。
     */
    private Integer showOrder;

    /**
     * 创建者Id。
     */
    private Long createUserId;

    /**
     * 更新者Id。
     */
    private Long updateUserId;

    /**
     * 创建时间。
     */
    private Date createTime;

    /**
     * 更新时间。
     */
    private Date updateTime;
}
