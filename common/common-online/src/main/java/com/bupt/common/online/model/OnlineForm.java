package com.bupt.common.online.model;

import com.baomidou.mybatisplus.annotation.*;
import com.bupt.common.core.annotation.*;
import com.bupt.common.core.base.mapper.BaseModelMapper;
import com.bupt.common.online.model.constant.FormType;
import com.bupt.common.online.vo.OnlineFormVo;
import lombok.Data;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Date;
import java.util.Map;

/**
 * 在线表单实体对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
@TableName(value = "zz_online_form")
public class OnlineForm {

    /**
     * 主键Id。
     */
    @TableId(value = "form_id")
    private Long formId;

    /**
     * 租户Id。
     */
    @TableField(value = "tenant_id")
    private Long tenantId;

    /**
     * 应用编码。为空时，表示非第三方应用接入。
     */
    @TableField(value = "app_code")
    private String appCode;

    /**
     * 页面id。
     */
    @TableField(value = "page_id")
    private Long pageId;

    /**
     * 表单编码。
     */
    @TableField(value = "form_code")
    private String formCode;

    /**
     * 表单名称。
     */
    @TableField(value = "form_name")
    private String formName;

    /**
     * 表单类别。
     */
    @TableField(value = "form_kind")
    private Integer formKind;

    /**
     * 表单类型。
     */
    @TableField(value = "form_type")
    private Integer formType;

    /**
     * 表单主表id。
     */
    @TableField(value = "master_table_id")
    private Long masterTableId;

    /**
     * 表单组件JSON。
     */
    @TableField(value = "widget_json")
    private String widgetJson;

    /**
     * 表单参数JSON。
     */
    @TableField(value = "params_json")
    private String paramsJson;

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

    @RelationOneToOne(
            masterIdField = "masterTableId",
            slaveModelClass = OnlineTable.class,
            slaveIdField = "tableId")
    @TableField(exist = false)
    private OnlineTable onlineTable;

    @RelationDict(
            masterIdField = "masterTableId",
            equalOneToOneRelationField = "onlineTable",
            slaveModelClass = OnlineTable.class,
            slaveIdField = "tableId",
            slaveNameField = "modelName")
    @TableField(exist = false)
    private Map<String, Object> masterTableIdDictMap;

    @RelationConstDict(
            masterIdField = "formType",
            constantDictClass = FormType.class)
    @TableField(exist = false)
    private Map<String, Object> formTypeDictMap;

    @Mapper
    public interface OnlineFormModelMapper extends BaseModelMapper<OnlineFormVo, OnlineForm> {
        /**
         * 转换Vo对象到实体对象。
         *
         * @param onlineFormVo 域对象。
         * @return 实体对象。
         */
        @Mapping(target = "onlineTable", expression = "java(mapToBean(onlineFormVo.getOnlineTable(), com.bupt.common.online.model.OnlineTable.class))")
        @Override
        OnlineForm toModel(OnlineFormVo onlineFormVo);
        /**
         * 转换实体对象到VO对象。
         *
         * @param onlineForm 实体对象。
         * @return 域对象。
         */
        @Mapping(target = "onlineTable", expression = "java(beanToMap(onlineForm.getOnlineTable(), false))")
        @Override
        OnlineFormVo fromModel(OnlineForm onlineForm);
    }
    public static final OnlineFormModelMapper INSTANCE = Mappers.getMapper(OnlineFormModelMapper.class);
}
