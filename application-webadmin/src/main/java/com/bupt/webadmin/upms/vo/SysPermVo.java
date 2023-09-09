package com.bupt.webadmin.upms.vo;

import com.bupt.common.core.base.vo.BaseVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 权限资源VO。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysPermVo extends BaseVo {

    /**
     * 权限资源Id。
     */
    private Long permId;

    /**
     * 权限资源名称。
     */
    private String permName;

    /**
     * shiro格式的权限字，如(upms:sysUser:add)。
     */
    private String permCode;

    /**
     * 权限所在的权限模块Id。
     */
    private Long moduleId;

    /**
     * 关联的URL。
     */
    private String url;

    /**
     * 权限在当前模块下的顺序，由小到大。
     */
    private Integer showOrder;

    /**
     * 模块Id的字典关联数据。
     */
    private Map<String, Object> moduleIdDictMap;
}