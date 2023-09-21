package com.bupt.common.ext.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * common-ext通用扩展模块的自动配置引导类。
 *
 * @author zzh
 * @date 2023-08-10
 */
@EnableConfigurationProperties({CommonExtProperties.class})
public class CommonExtAutoConfig {
}
