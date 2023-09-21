package com.bupt.common.core.object;

import lombok.Data;

/**
 * 数据表模型基础信息。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
public class TableModelInfo {

    /**
     * 数据表名。
     */
    private String tableName;

    /**
     * 实体对象名。
     */
    private String modelName;

    /**
     * 主键的表字段名。
     */
    private String keyColumnName;

    /**
     * 主键在实体对象中的属性名。
     */
    private String keyFieldName;
}
