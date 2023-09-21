package com.bupt.webadmin.app.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 行政区划实体对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
@TableName(value = "zz_area_code")
public class AreaCode {

    /**
     * 行政区划主键Id
     */
    @TableId(value = "area_id")
    private Long areaId;

    /**
     * 行政区划名称
     */
    @TableField(value = "area_name")
    private String areaName;

    /**
     * 行政区划级别 (1: 省级别 2: 市级别 3: 区级别)
     */
    @TableField(value = "area_level")
    private Integer areaLevel;

    /**
     * 父级行政区划Id
     */
    @TableField(value = "parent_id")
    private Long parentId;
}