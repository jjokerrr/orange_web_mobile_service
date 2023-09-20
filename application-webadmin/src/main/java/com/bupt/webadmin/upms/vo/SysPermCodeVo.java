package com.bupt.webadmin.upms.vo;

import com.bupt.common.core.base.vo.BaseVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * 权限字VO。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysPermCodeVo extends BaseVo {

    /**
     * 权限字Id。
     */
    private Long permCodeId;

    /**
     * 权限字标识(一般为有含义的英文字符串)。
     */
    private String permCode;

    /**
     * 上级权限字Id。
     */
    private Long parentId;

    /**
     * 权限字类型(0: 表单 1: UI片段 2: 操作)。
     */
    private Integer permCodeType;

    /**
     * 显示名称。
     */
    private String showName;

    /**
     * 显示顺序(数值越小，越靠前)。
     */
    private Integer showOrder;

    /**
     * 权限字与权限资源关联对象列表。
     */
    private List<Map<String, Object>> sysPermCodePermList;
}