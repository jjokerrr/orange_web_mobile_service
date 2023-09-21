package com.bupt.common.log.annotation;

import java.lang.annotation.*;

/**
 * 忽略接口应答数据记录日志的注解。该注解会被OperationLogAspect处理。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreResponseLog {

}
