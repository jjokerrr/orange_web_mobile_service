package com.bupt.common.mobile.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 移动端的配置对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
@ConfigurationProperties(prefix = "common-mobile")
public class MobileProperties {

    /**
     * 在线表单业务操作的URL前缀。
     */
    private String urlPrefix;
    /**
     * 上传文件的根路径。
     */
    private String uploadFileBaseDir;
}
