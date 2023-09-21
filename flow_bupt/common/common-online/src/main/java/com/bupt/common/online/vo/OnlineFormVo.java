package com.bupt.common.online.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 在线表单VO对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
public class OnlineFormVo {

    /**
     * 主键Id。
     */
    private Long formId;

    /**
     * 应用编码。为空时，表示非第三方应用接入。
     */
    private String appCode;

    /**
     * 页面Id。
     */
    private Long pageId;

    /**
     * 表单编码。
     */
    private String formCode;

    /**
     * 表单名称。
     */
    private String formName;

    /**
     * 表单类型。
     */
    private Integer formType;

    /**
     * 表单类别。
     */
    private Integer formKind;

    /**
     * 表单主表Id。
     */
    private Long masterTableId;

    /**
     * 表单组件JSON。
     */
    private String widgetJson;

    /**
     * 表单参数JSON。
     */
    private String paramsJson;

    /**
     * 创建时间。
     */
    private Date createTime;

    /**
     * 创建者。
     */
    private Long createUserId;

    /**
     * 更新时间。
     */
    private Date updateTime;

    /**
     * 更新者。
     */
    private Long updateUserId;

    /**
     * masterTableId 的一对一关联数据对象，数据对应类型为OnlineTableVo。
     */
    private Map<String, Object> onlineTable;

    /**
     * masterTableId 字典关联数据。
     */
    private Map<String, Object> masterTableIdDictMap;

    /**
     * formType 常量字典关联数据。
     */
    private Map<String, Object> formTypeDictMap;

    /**
     * 当前表单关联的数据源Id集合。
     */
    private List<Long> datasourceIdList;
}
