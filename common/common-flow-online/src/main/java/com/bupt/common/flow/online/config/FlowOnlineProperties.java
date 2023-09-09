package com.bupt.common.flow.online.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 在线表单工作流模块的配置对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
@ConfigurationProperties(prefix = "common-flow-online")
public class FlowOnlineProperties {

    /**
     * 在线表单的URL前缀。
     */
    private String urlPrefix;
}
