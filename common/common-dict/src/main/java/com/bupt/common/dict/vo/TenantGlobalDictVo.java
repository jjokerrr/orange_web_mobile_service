package com.bupt.common.dict.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租户全局系统字典Vo。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TenantGlobalDictVo extends GlobalDictVo {

    /**
     * 是否为所有租户的通用字典。
     */
    private Boolean tenantCommon;

    /**
     * 租户的非公用字典的初始化字典数据。
     */
    private String initialData;
}
