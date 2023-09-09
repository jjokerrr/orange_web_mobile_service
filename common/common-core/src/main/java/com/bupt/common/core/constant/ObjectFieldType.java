package com.bupt.common.core.constant;

/**
 * 对应于数据表字段中的类型，我们需要统一映射到Java实体对象字段的类型。
 * 该类是描述Java实体对象字段类型的常量类。
 *
 * @author zzh
 * @date 2023-08-10
 */
public final class ObjectFieldType {

    public static final String LONG = "Long";
    public static final String INTEGER = "Integer";
    public static final String DOUBLE = "Double";
    public static final String BIG_DECIMAL = "BigDecimal";
    public static final String BOOLEAN = "Boolean";
    public static final String STRING = "String";
    public static final String DATE = "Date";
    public static final String BYTE_ARRAY = "byte[]";

    /**
     * 私有构造函数，明确标识该常量类的作用。
     */
    private ObjectFieldType() {
    }
}
