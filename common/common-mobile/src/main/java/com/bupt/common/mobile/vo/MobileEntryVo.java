package com.bupt.common.mobile.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 移动端入口视图对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
public class MobileEntryVo {

    /**
     * 主键Id。
     */
    private Long entryId;

    /**
     * 父Id。
     */
    private Long parentId;

    /**
     * 显示名称。
     */
    private String entryName;

    /**
     * 移动端入口类型。
     */
    private Integer entryType;

    /**
     * 是否对所有角色可见。
     */
    private Boolean commonEntry;

    /**
     * 附件信息。
     */
    private String extraData;

    /**
     * 显示图片。
     */
    private String imageData;

    /**
     * 显示顺序。
     */
    private Integer showOrder;

    /**
     * 创建者Id。
     */
    private Long createUserId;

    /**
     * 创建时间。
     */
    private Date createTime;

    /**
     * 更新者Id。
     */
    private Long updateUserId;

    /**
     * 最后更新时间。
     */
    private Date updateTime;

    /**
     * 多对多移动端入口和角色数据集合。
     */
    private List<Map<String, Object>> mobileEntryRoleList;
}
