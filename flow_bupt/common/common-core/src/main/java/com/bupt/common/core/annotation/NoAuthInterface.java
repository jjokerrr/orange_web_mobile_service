package com.bupt.common.core.annotation;

import java.lang.annotation.*;

/**
 * 主要用于标记无需Token验证的接口
 *
 * @author zzh
 * @date 2023-08-10
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NoAuthInterface {
}
