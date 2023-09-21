package com.bupt.common.online.vo;

import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * 在线表单数据表字段验证规则VO对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
public class OnlineRuleVo {

    /**
     * 主键Id。
     */
    private Long ruleId;

    /**
     * 应用编码。为空时，表示非第三方应用接入。
     */
    private String appCode;

    /**
     * 规则名称。
     */
    private String ruleName;

    /**
     * 规则类型。
     */
    private Integer ruleType;

    /**
     * 内置规则标记。
     */
    private Boolean builtin;

    /**
     * 自定义规则的正则表达式。
     */
    private String pattern;

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
     * ruleId 的多对多关联表数据对象，数据对应类型为OnlineColumnRuleVo。
     */
    private Map<String, Object> onlineColumnRule;

    /**
     * ruleType 常量字典关联数据。
     */
    private Map<String, Object> ruleTypeDictMap;
}
