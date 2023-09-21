package com.bupt.common.online.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 在线表单的配置对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
@ConfigurationProperties(prefix = "common-online")
public class OnlineProperties {

    /**
     * 脱敏字段的掩码。只能为单个字符。
     */
    private String maskChar = "*";
    /**
     * 在调用render接口的时候，是否打开一级缓存加速页面渲染数据的获取。
     */
    private Boolean enableRenderCache = true;
    /**
     * 仅以该前缀开头的数据表才会成为动态表单的候选数据表，如: zz_。如果为空，则所有表均可被选。
     */
    private String tablePrefix;
    /**
     * 在线表单业务操作的URL前缀。
     */
    private String urlPrefix;
    /**
     * 在线表单打印接口的路径
     */
    private String printUrlPath;
    /**
     * 上传文件的根路径。
     */
    private String uploadFileBaseDir;
    /**
     * 1: minio 2: aliyun-oss 3: qcloud-cos。
     * 0是本地系统，不推荐使用。
     */
    private Integer distributeStoreType;
    /**
     * 在线表单查看权限的URL列表。
     */
    private List<String> viewUrlList;
    /**
     * 在线表单编辑权限的URL列表。
     */
    private List<String> editUrlList;
}
