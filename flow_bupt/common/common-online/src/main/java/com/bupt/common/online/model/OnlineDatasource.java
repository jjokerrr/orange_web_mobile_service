package com.bupt.common.online.model;

import com.baomidou.mybatisplus.annotation.*;
import com.bupt.common.core.annotation.RelationDict;
import com.bupt.common.core.base.mapper.BaseModelMapper;
import com.bupt.common.online.vo.OnlineDatasourceVo;
import lombok.Data;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 在线表单的数据源实体对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
@TableName(value = "zz_online_datasource")
public class OnlineDatasource {

    /**
     * 主键Id。
     */
    @TableId(value = "datasource_id")
    private Long datasourceId;

    /**
     * 应用编码。为空时，表示非第三方应用接入。
     */
    @TableField(value = "app_code")
    private String appCode;

    /**
     * 数据源名称。
     */
    @TableField(value = "datasource_name")
    private String datasourceName;

    /**
     * 数据源变量名，会成为数据访问url的一部分。
     */
    @TableField(value = "variable_name")
    private String variableName;

    /**
     * 数据库链接Id。
     */
    @TableField(value = "dblink_id")
    private Long dblinkId;

    /**
     * 主表Id。
     */
    @TableField(value = "master_table_id")
    private Long masterTableId;

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
     * datasourceId 的多对多关联的数据对象。
     */
    @TableField(exist = false)
    private OnlinePageDatasource onlinePageDatasource;

    /**
     * datasourceId 的多对多关联的数据对象。
     */
    @TableField(exist = false)
    private List<OnlineFormDatasource> onlineFormDatasourceList;

    @RelationDict(
            masterIdField = "masterTableId",
            slaveModelClass = OnlineTable.class,
            slaveIdField = "tableId",
            slaveNameField = "tableName")
    @TableField(exist = false)
    private Map<String, Object> masterTableIdDictMap;

    @TableField(exist = false)
    private OnlineTable masterTable;

    @Mapper
    public interface OnlineDatasourceModelMapper extends BaseModelMapper<OnlineDatasourceVo, OnlineDatasource> {
        /**
         * 转换Vo对象到实体对象。
         *
         * @param onlineDatasourceVo 域对象。
         * @return 实体对象。
         */
        @Mapping(target = "onlinePageDatasource", expression = "java(mapToBean(onlineDatasourceVo.getOnlinePageDatasource(), com.bupt.common.online.model.OnlinePageDatasource.class))")
        @Override
        OnlineDatasource toModel(OnlineDatasourceVo onlineDatasourceVo);
        /**
         * 转换实体对象到VO对象。
         *
         * @param onlineDatasource 实体对象。
         * @return 域对象。
         */
        @Mapping(target = "onlinePageDatasource", expression = "java(beanToMap(onlineDatasource.getOnlinePageDatasource(), false))")
        @Override
        OnlineDatasourceVo fromModel(OnlineDatasource onlineDatasource);
    }
    public static final OnlineDatasourceModelMapper INSTANCE = Mappers.getMapper(OnlineDatasourceModelMapper.class);
}
