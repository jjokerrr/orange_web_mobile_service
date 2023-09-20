package com.bupt.webadmin.upms.model;

import com.baomidou.mybatisplus.annotation.*;
import com.bupt.common.core.annotation.RelationManyToMany;
import com.bupt.common.core.base.model.BaseModel;
import com.bupt.common.core.base.mapper.BaseModelMapper;
import com.bupt.webadmin.upms.vo.SysPermCodeVo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.*;

/**
 * 权限字实体对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "bupt_sys_perm_code")
public class SysPermCode extends BaseModel {

    /**
     * 权限字Id。
     */
    @TableId(value = "perm_code_id")
    private Long permCodeId;

    /**
     * 上级权限字Id。
     */
    @TableField(value = "parent_id")
    private Long parentId;

    /**
     * 权限字标识(一般为有含义的英文字符串)。
     */
    @TableField(value = "perm_code")
    private String permCode;

    /**
     * 权限类型(0: 表单 1: UI片段 2: 操作)。
     */
    @TableField(value = "perm_code_type")
    private Integer permCodeType;

    /**
     * 显示名称。
     */
    @TableField(value = "show_name")
    private String showName;

    /**
     * 显示顺序(数值越小，越靠前)。
     */
    @TableField(value = "show_order")
    private Integer showOrder;

    @RelationManyToMany(
            relationMasterIdField = "permCodeId",
            relationModelClass = SysPermCodePerm.class)
    @TableField(exist = false)
    private List<SysPermCodePerm> sysPermCodePermList;

    @Mapper
    public interface SysPermCodeModelMapper extends BaseModelMapper<SysPermCodeVo, SysPermCode> {
        /**
         * 转换VO对象到实体对象。
         *
         * @param sysPermCodeVo 域对象。
         * @return 实体对象。
         */
        @Mapping(target = "sysPermCodePermList", expression = "java(mapToBean(sysPermCodeVo.getSysPermCodePermList(), com.bupt.webadmin.upms.model.SysPermCodePerm.class))")
        @Override
        SysPermCode toModel(SysPermCodeVo sysPermCodeVo);
        /**
         * 转换实体对象到VO对象。
         *
         * @param sysPermCode 实体对象。
         * @return 域对象。
         */
        @Mapping(target = "sysPermCodePermList", expression = "java(beanToMap(sysPermCode.getSysPermCodePermList(), false))")
        @Override
        SysPermCodeVo fromModel(SysPermCode sysPermCode);
    }
    public static final SysPermCodeModelMapper INSTANCE = Mappers.getMapper(SysPermCode.SysPermCodeModelMapper.class);
}
