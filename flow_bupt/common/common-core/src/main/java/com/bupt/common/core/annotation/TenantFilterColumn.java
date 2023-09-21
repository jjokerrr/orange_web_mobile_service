package com.bupt.common.core.annotation;

import java.lang.annotation.*;

/**
 * 主要用于标记通过租户Id进行过滤的字段。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TenantFilterColumn {

}
