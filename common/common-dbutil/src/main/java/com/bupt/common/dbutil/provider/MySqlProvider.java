package com.bupt.common.dbutil.provider;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.bupt.common.core.constant.ObjectFieldType;
import com.bupt.common.dbutil.constant.DblinkType;

/**
 * MySQL数据源的提供者实现类。
 *
 * @author zzh
 * @date 2023-08-10
 */
public class MySqlProvider implements DataSourceProvider {

    @Override
    public int getDblinkType() {
        return DblinkType.MYSQL;
    }

    @Override
    public JdbcConfig getJdbcConfig(String configuration) {
        return JSON.parseObject(configuration, MySqlConfig.class);
    }

    @Override
    public String getTableMetaListSql(String searchString) {
        StringBuilder sql = new StringBuilder();
        sql.append(this.getTableMetaListSql());
        if (StrUtil.isNotBlank(searchString)) {
            sql.append(" AND table_name LIKE ?");
        }
        return sql.append(" ORDER BY table_name").toString();
    }

    @Override
    public String getTableMetaSql() {
        return this.getTableMetaListSql() + " AND table_name = ?";
    }

    @Override
    public String getTableColumnMetaListSql() {
        return "SELECT "
                + "    column_name columnName, "
                + "    data_type columnType, "
                + "    column_type fullColumnType, "
                + "    column_comment columnComment, "
                + "    CASE WHEN column_key = 'PRI' THEN 1 ELSE 0 END AS primaryKey, "
                + "    is_nullable nullable, "
                + "    ordinal_position columnShowOrder, "
                + "    extra extra, "
                + "    CHARACTER_MAXIMUM_LENGTH stringPrecision, "
                + "    numeric_precision numericPrecision, "
                + "    COLUMN_DEFAULT columnDefault "
                + "FROM "
                + "    information_schema.columns "
                + "WHERE "
                + "    table_name = ?"
                + "    AND table_schema = (SELECT database()) "
                + "ORDER BY ordinal_position";
    }

    @Override
    public String makePageSql(String sql, Integer pageNum, Integer pageSize) {
        if (pageSize == null) {
            pageSize = 10;
        }
        int offset = pageNum > 0 ? (pageNum - 1) * pageSize : 0;
        return sql + " LIMIT " + offset + "," + pageSize;
    }

    @Override
    public String convertColumnTypeToJavaType(String columnType, Integer numericPrecision, Integer numericScale) {
        if (StrUtil.equalsAnyIgnoreCase(columnType,
                "varchar", "char", "text", "longtext", "mediumtext", "tinytext", "enum", "json")) {
            return ObjectFieldType.STRING;
        }
        if (StrUtil.equalsAnyIgnoreCase(columnType, "int", "mediumint", "smallint", "tinyint")) {
            return ObjectFieldType.INTEGER;
        }
        if (StrUtil.equalsIgnoreCase(columnType, "bit")) {
            return ObjectFieldType.BOOLEAN;
        }
        if (StrUtil.equalsIgnoreCase(columnType, "bigint")) {
            return ObjectFieldType.LONG;
        }
        if (StrUtil.equalsIgnoreCase(columnType, "decimal")) {
            return ObjectFieldType.BIG_DECIMAL;
        }
        if (StrUtil.equalsAnyIgnoreCase(columnType, "float", "double")) {
            return ObjectFieldType.DOUBLE;
        }
        if (StrUtil.equalsAnyIgnoreCase(columnType, "date", "datetime", "timestamp", "time")) {
            return ObjectFieldType.DATE;
        }
        if (StrUtil.equalsAnyIgnoreCase(columnType, "longblob", "blob")) {
            return ObjectFieldType.BYTE_ARRAY;
        }
        return null;
    }

    private String getTableMetaListSql() {
        return  "SELECT "
                + "    table_name tableName, "
                + "    table_comment tableComment, "
                + "    create_time createTime "
                + "FROM "
                + "    information_schema.tables "
                + "WHERE "
                + "    table_schema = (SELECT database()) ";
    }
}
