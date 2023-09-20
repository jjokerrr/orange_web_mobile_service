package com.bupt.webadmin.upms.model;

import com.baomidou.mybatisplus.annotation.*;
import com.bupt.common.core.base.model.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.*;

/**
 * 权限模块实体对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "bupt_sys_perm_module")
public class SysPermModule extends BaseModel {

    /**
     * 权限模块Id。
     */
    @TableId(value = "module_id")
    private Long moduleId;

    /**
     * 上级权限模块Id。
     */
    @TableField(value = "parent_id")
    private Long parentId;

    /**
     * 权限模块名称。
     */
    @TableField(value = "module_name")
    private String moduleName;

    /**
     * 权限模块类型(0: 普通模块 1: Controller模块)。
     */
    @TableField(value = "module_type")
    private Integer moduleType;

    /**
     * 权限模块在当前层级下的顺序，由小到大。
     */
    @TableField(value = "show_order")
    private Integer showOrder;

    @TableField(exist = false)
    private List<SysPerm> sysPermList;
}
