package com.bupt.common.mobile.model;

import com.baomidou.mybatisplus.annotation.*;
import com.bupt.common.core.annotation.RelationManyToMany;
import com.bupt.common.core.annotation.UploadFlagColumn;
import com.bupt.common.core.base.mapper.BaseModelMapper;
import com.bupt.common.core.upload.UploadStoreTypeEnum;
import com.bupt.common.mobile.object.MobileEntryExtraData;
import com.bupt.common.mobile.vo.MobileEntryVo;
import lombok.Data;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.Date;
import java.util.List;

/**
 * 移动端入口实体对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
@TableName(value = "zz_mobile_entry")
public class MobileEntry {

    /**
     * 主键Id。
     */
    @TableId(value = "entry_id")
    private Long entryId;

    /**
     * 租户Id。
     */
    @TableField(value = "tenant_id")
    private Long tenantId;

    /**
     * 父Id。
     */
    @TableField(value = "parent_id")
    private Long parentId;

    /**
     * 显示名称。
     */
    @TableField(value = "entry_name")
    private String entryName;

    /**
     * 入口类型，具体值可参考MobileEntryType常量类。
     */
    @TableField(value = "entry_type")
    private Integer entryType;

    /**
     * 是否对所有角色可见。
     */
    @TableField(value = "common_entry")
    private Boolean commonEntry;

    /**
     * 附件信息。
     */
    @TableField(value = "extra_data")
    private String extraData;

    /**
     * 显示图片。
     */
    @UploadFlagColumn(storeType = UploadStoreTypeEnum.LOCAL_SYSTEM)
    @TableField(value = "image_data")
    private String imageData;

    /**
     * 显示顺序。
     */
    @TableField(value = "show_order")
    private Integer showOrder;

    /**
     * 创建时间。
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 创建者。
     */
    @TableField(value = "create_user_id")
    private Long createUserId;

    /**
     * 更新时间。
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 更新者。
     */
    @TableField(value = "update_user_id")
    private Long updateUserId;

    @TableField(exist = false)
    private MobileEntryExtraData extraObject;

    @RelationManyToMany(
            relationMasterIdField = "entryId",
            relationModelClass = MobileEntryRole.class)
    @TableField(exist = false)
    private List<MobileEntryRole> mobileEntryRoleList;

    @Mapper
    public interface MobileEntryModelMapper extends BaseModelMapper<MobileEntryVo, MobileEntry> {
        /**
         * 转换VO对象到实体对象。
         *
         * @param mobileEntryVo 域对象。
         * @return 实体对象。
         */
        @Mapping(target = "mobileEntryRoleList", expression = "java(mapToBean(mobileEntryVo.getMobileEntryRoleList(), com.bupt.common.mobile.model.MobileEntryRole.class))")
        @Override
        MobileEntry toModel(MobileEntryVo mobileEntryVo);
        /**
         * 转换实体对象到VO对象。
         *
         * @param mobileEntry 实体对象。
         * @return 域对象。
         */
        @Mapping(target = "mobileEntryRoleList", expression = "java(beanToMap(mobileEntry.getMobileEntryRoleList(), false))")
        @Override
        MobileEntryVo fromModel(MobileEntry mobileEntry);
    }
    public static final MobileEntryModelMapper INSTANCE = Mappers.getMapper(MobileEntryModelMapper.class);
}
