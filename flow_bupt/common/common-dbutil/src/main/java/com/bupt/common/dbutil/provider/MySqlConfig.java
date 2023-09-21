package com.bupt.common.dbutil.provider;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * MySQL JDBC配置。
 *
 * @author zzh
 * @date 2023-08-10
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MySqlConfig extends JdbcConfig {

    /**
     * JDBC 驱动名。
     */
    private String driver = "com.mysql.cj.jdbc.Driver";
    /**
     * 数据库JDBC连接串的扩展部分。
     */
    private String extraParams = "?characterEncoding=utf8&useSSL=true&serverTimezone=Asia/Shanghai";

    /**
     * 获取拼好后的JDBC连接串。
     *
     * @return 拼好后的JDBC连接串。
     */
    @Override
    public String getJdbcConnectionString() {
        StringBuilder sb = new StringBuilder(256);
        sb.append("jdbc:mysql://")
                .append(getHost())
                .append(":")
                .append(getPort())
                .append("/")
                .append(getDatabase())
                .append(extraParams);
        return sb.toString();
    }
}
