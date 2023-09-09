package com.bupt.common.core.annotation;

import com.bupt.common.core.util.DataSourceResolver;

import java.lang.annotation.*;

/**
 * 基于自定义解析规则的多数据源注解。主要用于标注Service的实现类。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyDataSourceResolver {

    /**
     * 多数据源路由键解析接口的Class。
     * @return 多数据源路由键解析接口的Class。
     */
    Class<? extends DataSourceResolver> resolver();

    /**
     * DataSourceResolver.resovle方法的入参。
     * @return DataSourceResolver.resovle方法的入参。
     */
    String arg() default "";

    /**
     * 数值型参数。
     * @return DataSourceResolver.resovle方法的入参。
     */
    int intArg() default -1;
}
