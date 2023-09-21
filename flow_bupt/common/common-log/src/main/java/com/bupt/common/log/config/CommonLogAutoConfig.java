package com.bupt.common.log.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * common-log模块的自动配置引导类。
 *
 * @author zzh
 * @date 2023-08-10
 */
@EnableConfigurationProperties({OperationLogProperties.class})
public class CommonLogAutoConfig {
}
