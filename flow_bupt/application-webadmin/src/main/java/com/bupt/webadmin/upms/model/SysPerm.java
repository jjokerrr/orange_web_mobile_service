package com.bupt.webadmin.upms.model;

import com.baomidou.mybatisplus.annotation.*;
import com.bupt.common.core.base.model.BaseModel;
import com.bupt.common.core.annotation.RelationDict;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.*;

/**
 * 权限资源实体对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "bupt_sys_perm")
public class SysPerm extends BaseModel {

    /**
     * 权限资源Id。
     */
    @TableId(value = "perm_id")
    private Long permId;

    /**
     * 权限所在的权限模块Id。
     */
    @TableField(value = "module_id")
    private Long moduleId;

    /**
     * 权限名称。
     */
    @TableField(value = "perm_name")
    private String permName;

    /**
     * 关联的URL。
     */
    private String url;

    /**
     * 权限在当前模块下的顺序，由小到大。
     */
    @TableField(value = "show_order")
    private Integer showOrder;

    @RelationDict(
            masterIdField = "moduleId",
            slaveModelClass = SysPermModule.class,
            slaveIdField = "moduleId",
            slaveNameField = "moduleName")
    @TableField(exist = false)
    private Map<String, Object> moduleIdDictMap;
}
