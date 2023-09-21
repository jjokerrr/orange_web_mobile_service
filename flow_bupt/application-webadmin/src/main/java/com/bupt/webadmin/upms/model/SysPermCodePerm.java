package com.bupt.webadmin.upms.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 权限字与权限资源关联实体对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
@TableName(value = "bupt_sys_perm_code_perm")
public class SysPermCodePerm {

    /**
     * 权限字Id。
     */
    @TableField(value = "perm_code_id")
    private Long permCodeId;

    /**
     * 权限Id。
     */
    @TableField(value = "perm_id")
    private Long permId;
}
