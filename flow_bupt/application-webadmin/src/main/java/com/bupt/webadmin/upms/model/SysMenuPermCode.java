package com.bupt.webadmin.upms.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 菜单与权限字关联实体对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
@TableName(value = "bupt_sys_menu_perm_code")
public class SysMenuPermCode {

    /**
     * 关联菜单Id。
     */
    @TableField(value = "menu_id")
    private Long menuId;

    /**
     * 关联权限字Id。
     */
    @TableField(value = "perm_code_id")
    private Long permCodeId;
}
