package com.bupt.common.core.util;

/**
 * 基于自定义解析规则的多数据源解析接口。
 *
 * @author zzh
 * @date 2023-08-10
 */
public interface DataSourceResolver {

    /**
     * 动态解析方法。实现类可以根据当前的请求，或者上下文环境进行动态解析。
     *
     * @param arg        可选的入参。MyDataSourceResolver注解中的arg参数。
     * @param intArg     可选的整型入参。MyDataSourceResolver注解中的intArg参数。
     * @param methodArgs 被织入方法的所有参数。
     * @return 返回用于多数据源切换的类型值。DataSourceResolveAspect 切面方法会根据该返回值和配置信息，进行多数据源切换。
     */
    Integer resolve(String arg, Integer intArg, Object[] methodArgs);
}
