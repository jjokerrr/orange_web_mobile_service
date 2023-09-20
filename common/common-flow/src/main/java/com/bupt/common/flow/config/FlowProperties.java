package com.bupt.common.flow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 工作流的配置对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
@ConfigurationProperties(prefix = "common-flow")
public class FlowProperties {

    /**
     * 工作落工单操作接口的URL前缀。
     */
    private String urlPrefix;
}
