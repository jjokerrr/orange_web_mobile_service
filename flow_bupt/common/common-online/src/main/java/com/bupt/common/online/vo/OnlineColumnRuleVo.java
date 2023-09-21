package com.bupt.common.online.vo;

import lombok.Data;

/**
 * 在线表单数据表字段规则和字段多对多关联VO对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
public class OnlineColumnRuleVo {

    /**
     * 字段Id。
     */
    private Long columnId;

    /**
     * 规则Id。
     */
    private Long ruleId;

    /**
     * 规则属性数据。
     */
    private String propDataJson;
}
