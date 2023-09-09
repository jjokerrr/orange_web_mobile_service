package com.bupt.webadmin.upms.vo;

import com.bupt.common.core.base.vo.BaseVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 权限资源模块VO。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysPermModuleVo extends BaseVo {

    /**
     * 权限模块Id。
     */
    private Long moduleId;

    /**
     * 权限模块名称。
     */
    private String moduleName;

    /**
     * 上级权限模块Id。
     */
    private Long parentId;

    /**
     * 权限模块类型(0: 普通模块 1: Controller模块)。
     */
    private Integer moduleType;

    /**
     * 权限模块在当前层级下的顺序，由小到大。
     */
    private Integer showOrder;

    /**
     * 权限资源对象列表。
     */
    private List<SysPermVo> sysPermList;
}