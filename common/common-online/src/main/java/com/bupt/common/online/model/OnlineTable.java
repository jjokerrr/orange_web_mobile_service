package com.bupt.common.online.model;

import com.baomidou.mybatisplus.annotation.*;
import com.bupt.common.core.annotation.RelationOneToMany;
import com.bupt.common.core.base.mapper.BaseModelMapper;
import com.bupt.common.online.vo.OnlineTableVo;
import lombok.Data;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 在线表单的数据表实体对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
@TableName(value = "zz_online_table")
public class OnlineTable {

    /**
     * 主键Id。
     */
    @TableId(value = "table_id")
    private Long tableId;

    /**
     * 应用编码。为空时，表示非第三方应用接入。
     */
    @TableField(value = "app_code")
    private String appCode;

    /**
     * 表名称。
     */
    @TableField(value = "table_name")
    private String tableName;

    /**
     * 实体名称。
     */
    @TableField(value = "model_name")
    private String modelName;

    /**
     * 数据库链接Id。
     */
    @TableField(value = "dblink_id")
    private Long dblinkId;

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

    @RelationOneToMany(
            masterIdField = "tableId",
            slaveModelClass = OnlineColumn.class,
            slaveIdField = "tableId")
    @TableField(exist = false)
    private List<OnlineColumn> columnList;

    /**
     * 该字段会被缓存，因此在线表单执行操作时可以从缓存中读取该数据，并可基于columnId进行快速检索。
     */
    @TableField(exist = false)
    private Map<Long, OnlineColumn> columnMap;

    /**
     * 当前表的主键字段，该字段仅仅用于动态表单运行时的SQL拼装。
     */
    @TableField(exist = false)
    private OnlineColumn primaryKeyColumn;

    /**
     * 当前表的逻辑删除字段，该字段仅仅用于动态表单运行时的SQL拼装。
     */
    @TableField(exist = false)
    private OnlineColumn logicDeleteColumn;

    @Mapper
    public interface OnlineTableModelMapper extends BaseModelMapper<OnlineTableVo, OnlineTable> {
        /**
         * 转换Vo对象到实体对象。
         *
         * @param onlineTableVo 域对象。
         * @return 实体对象。
         */
        @Override
        OnlineTable toModel(OnlineTableVo onlineTableVo);
        /**
         * 转换实体对象到VO对象。
         *
         * @param onlineTable 实体对象。
         * @return 域对象。
         */
        @Override
        OnlineTableVo fromModel(OnlineTable onlineTable);
    }
    public static final OnlineTableModelMapper INSTANCE = Mappers.getMapper(OnlineTableModelMapper.class);
}
