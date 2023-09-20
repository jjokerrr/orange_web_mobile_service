package com.bupt.common.online.model;

import com.baomidou.mybatisplus.annotation.*;
import com.bupt.common.core.annotation.RelationConstDict;
import com.bupt.common.core.base.mapper.BaseModelMapper;
import com.bupt.common.dbutil.constant.DblinkType;
import com.bupt.common.online.vo.OnlineDblinkVo;
import lombok.Data;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Date;
import java.util.Map;

/**
 * 在线表单数据表所在数据库链接实体对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
@TableName(value = "zz_online_dblink")
public class OnlineDblink {

    /**
     * 主键Id。
     */
    @TableId(value = "dblink_id")
    private Long dblinkId;

    /**
     * 应用编码。为空时，表示非第三方应用接入。
     */
    @TableField(value = "app_code")
    private String appCode;

    /**
     * 链接中文名称。
     */
    @TableField(value = "dblink_name")
    private String dblinkName;

    /**
     * 链接描述。
     */
    @TableField(value = "dblink_description")
    private String dblinkDescription;

    /**
     * 配置信息。
     */
    private String configuration;

    /**
     * 数据库链接类型。
     */
    @TableField(value = "dblink_type")
    private Integer dblinkType;

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
     * 修改时间。
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 更新者。
     */
    @TableField(value = "update_user_id")
    private Long updateUserId;

    @RelationConstDict(
            masterIdField = "dblinkType",
            constantDictClass = DblinkType.class)
    @TableField(exist = false)
    private Map<String, Object> dblinkTypeDictMap;

    @Mapper
    public interface OnlineDblinkModelMapper extends BaseModelMapper<OnlineDblinkVo, OnlineDblink> {
        /**
         * 转换Vo对象到实体对象。
         *
         * @param onlineDblinkVo 域对象。
         * @return 实体对象。
         */
        @Override
        OnlineDblink toModel(OnlineDblinkVo onlineDblinkVo);
        /**
         * 转换实体对象到VO对象。
         *
         * @param onlineDblink 实体对象。
         * @return 域对象。
         */
        @Override
        OnlineDblinkVo fromModel(OnlineDblink onlineDblink);
    }
    public static final OnlineDblinkModelMapper INSTANCE = Mappers.getMapper(OnlineDblinkModelMapper.class);
}
