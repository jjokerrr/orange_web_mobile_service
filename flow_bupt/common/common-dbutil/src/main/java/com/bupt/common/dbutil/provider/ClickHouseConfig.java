package com.bupt.common.dbutil.provider;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ClickHouse JDBC配置。
 *
 * @author zzh
 * @date 2023-08-10
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ClickHouseConfig extends JdbcConfig {

    /**
     * JDBC 驱动名。
     */
    private String driver = "ru.yandex.clickhouse.ClickHouseDriver";

    /**
     * 获取拼好后的JDBC连接串。
     *
     * @return 拼好后的JDBC连接串。
     */
    @Override
    public String getJdbcConnectionString() {
        StringBuilder sb = new StringBuilder(256);
        sb.append("jdbc:clickhouse://")
                .append(getHost())
                .append(":")
                .append(getPort())
                .append("/")
                .append(getDatabase());
        return sb.toString();
    }
}
