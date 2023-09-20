package com.bupt.webadmin.app.vo;

import lombok.Data;

/**
 * 行政区划DomainVO对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
public class AreaCodeVo {

    /**
     * 行政区划主键Id
     */
    private Long areaId;

    /**
     * 行政区划名称
     */
    private String areaName;

    /**
     * 行政区划级别 (1: 省级别 2: 市级别 3: 区级别)
     */
    private Integer areaLevel;

    /**
     * 父级行政区划Id
     */
    private Long parentId;
}