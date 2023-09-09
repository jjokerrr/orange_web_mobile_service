package com.bupt.common.dict.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租户全局系统字典Dto。
 *
 * @author zzh
 * @date 2023-08-10
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TenantGlobalDictDto extends GlobalDictDto {

    /**
     * 是否为所有租户的通用字典。
     */
    private Boolean tenantCommon;

    /**
     * 租户的非公用字典的初始化字典数据。
     */
    private String initialData;
}
