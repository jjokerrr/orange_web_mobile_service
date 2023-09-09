package com.bupt.common.online.model;

import com.baomidou.mybatisplus.annotation.*;
import com.bupt.common.core.annotation.RelationConstDict;
import com.bupt.common.core.annotation.RelationOneToOne;
import com.bupt.common.core.base.mapper.BaseModelMapper;
import com.bupt.common.online.model.constant.FieldKind;
import com.bupt.common.online.vo.OnlineColumnVo;
import lombok.Data;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Date;
import java.util.Map;

/**
 * 在线表单数据表字段实体对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
@TableName(value = "zz_online_column")
public class OnlineColumn {

    /**
     * 主键Id。
     */
    @TableId(value = "column_id")
    private Long columnId;

    /**
     * 字段名。
     */
    @TableField(value = "column_name")
    private String columnName;

    /**
     * 数据表Id。
     */
    @TableField(value = "table_id")
    private Long tableId;

    /**
     * 数据表中的字段类型。
     */
    @TableField(value = "column_type")
    private String columnType;

    /**
     * 数据表中的完整字段类型(包括了精度和刻度)。
     */
    @TableField(value = "full_column_type")
    private String fullColumnType;

    /**
     * 是否为主键。
     */
    @TableField(value = "primary_key")
    private Boolean primaryKey;

    /**
     * 是否是自增主键(0: 不是 1: 是)。
     */
    @TableField(value = "auto_incr")
    private Boolean autoIncrement;

    /**
     * 是否可以为空 (0: 不可以为空 1: 可以为空)。
     */
    @TableField(value = "nullable")
    private Boolean nullable;

    /**
     * 缺省值。
     */
    @TableField(value = "column_default")
    private String columnDefault;

    /**
     * 字段在数据表中的显示位置。
     */
    @TableField(value = "column_show_order")
    private Integer columnShowOrder;

    /**
     * 数据表中的字段注释。
     */
    @TableField(value = "column_comment")
    private String columnComment;

    /**
     * 对象映射字段名称。
     */
    @TableField(value = "object_field_name")
    private String objectFieldName;

    /**
     * 对象映射字段类型。
     */
    @TableField(value = "object_field_type")
    private String objectFieldType;

    /**
     * 数值型字段的精度(目前仅Oracle使用)。
     */
    @TableField(value = "numeric_precision")
    private Integer numericPrecision;

    /**
     * 数值型字段的刻度(小数点后位数，目前仅Oracle使用)。
     */
    @TableField(value = "numeric_scale")
    private Integer numericScale;

    /**
     * 过滤字段类型。
     */
    @TableField(value = "filter_type")
    private Integer filterType;

    /**
     * 是否是主键的父Id。
     */
    @TableField(value = "parent_key")
    private Boolean parentKey;

    /**
     * 是否部门过滤字段。
     */
    @TableField(value = "dept_filter")
    private Boolean deptFilter;

    /**
     * 是否用户过滤字段。
     */
    @TableField(value = "user_filter")
    private Boolean userFilter;

    /**
     * 字段类别。
     */
    @TableField(value = "field_kind")
    private Integer fieldKind;

    /**
     * 包含的文件文件数量，0表示无限制。
     */
    @TableField(value = "max_file_count")
    private Integer maxFileCount;

    /**
     * 上传文件系统类型。
     */
    @TableField(value = "upload_file_system_type")
    private Integer uploadFileSystemType;

    /**
     * 编码规则的JSON格式数据。
     */
    @TableField(value = "encoded_rule")
    private String encodedRule;

    /**
     * 脱敏字段类型，具体值可参考MaskFieldTypeEnum枚举。
     */
    @TableField(value = "mask_field_type")
    private String maskFieldType;

    /**
     * 字典Id。
     */
    @TableField(value = "dict_id")
    private Long dictId;

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

    /**
     * SQL查询时候使用的别名。
     */
    @TableField(exist = false)
    private String columnAliasName;

    @RelationConstDict(
            masterIdField = "fieldKind",
            constantDictClass = FieldKind.class)
    @TableField(exist = false)
    private Map<String, Object> fieldKindDictMap;

    @RelationOneToOne(
            masterIdField = "dictId",
            slaveModelClass = OnlineDict.class,
            slaveIdField = "dictId",
            loadSlaveDict = false)
    @TableField(exist = false)
    private OnlineDict dictInfo;

    @Mapper
    public interface OnlineColumnModelMapper extends BaseModelMapper<OnlineColumnVo, OnlineColumn> {
        /**
         * 转换Vo对象到实体对象。
         *
         * @param onlineColumnVo 域对象。
         * @return 实体对象。
         */
        @Mapping(target = "dictInfo", expression = "java(mapToBean(onlineColumnVo.getDictInfo(), com.bupt.common.online.model.OnlineDict.class))")
        @Override
        OnlineColumn toModel(OnlineColumnVo onlineColumnVo);
        /**
         * 转换实体对象到VO对象。
         *
         * @param onlineColumn 实体对象。
         * @return 域对象。
         */
        @Mapping(target = "dictInfo", expression = "java(beanToMap(onlineColumn.getDictInfo(), false))")
        @Override
        OnlineColumnVo fromModel(OnlineColumn onlineColumn);
    }
    public static final OnlineColumnModelMapper INSTANCE = Mappers.getMapper(OnlineColumnModelMapper.class);
}
