package com.bupt.common.core.annotation;

import java.lang.annotation.*;

/**
 * 业务表中记录流程实例结束标记的字段。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FlowStatusColumn {

}
