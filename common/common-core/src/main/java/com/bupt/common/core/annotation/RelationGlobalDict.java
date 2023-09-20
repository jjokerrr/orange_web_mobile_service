package com.bupt.common.core.annotation;

import java.lang.annotation.*;

/**
 * 全局字典关联。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RelationGlobalDict {

    /**
     * 当前对象的关联Id字段名称。
     *
     * @return 当前对象的关联Id字段名称。
     */
    String masterIdField();

    /**
     * 全局字典编码。
     *
     * @return 全局字典编码。空表示为不使用全局字典。
     */
    String dictCode();
}
