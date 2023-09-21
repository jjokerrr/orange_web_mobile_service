package com.bupt.webadmin.upms.vo;

import com.bupt.common.core.base.vo.BaseVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * 角色VO。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRoleVo extends BaseVo {

    /**
     * 角色Id。
     */
    private Long roleId;

    /**
     * 角色名称。
     */
    private String roleName;

    /**
     * 角色与菜单关联对象列表。
     */
    private List<Map<String, Object>> sysRoleMenuList;
}
