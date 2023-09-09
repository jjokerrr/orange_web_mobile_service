package com.bupt.common.online.util;

/**
 * 在线表单 Redis 键生成工具类。
 *
 * @author zzh
 * @date 2023-08-10
 */
public class OnlineRedisKeyUtil {

    /**
     * 计算在线表对象缓存在Redis中的键值。
     *
     * @param tableId 在线表主键Id。
     * @return 在线表对象缓存在Redis中的键值。
     */
    public static String makeOnlineTableKey(Long tableId) {
        return "ONLINE_TABLE:" + tableId;
    }

    /**
     * 计算在线表单对象缓存在Redis中的键值。
     *
     * @param formId 在线表单对象主键Id。
     * @return 在线表单对象缓存在Redis中的键值。
     */
    public static String makeOnlineFormKey(Long formId) {
        return "ONLINE_FORM:" + formId;
    }

    /**
     * 计算在线表单关联数据源对象列表缓存在Redis中的键值。
     *
     * @param formId 在线表单对象主键Id。
     * @return 在线表单关联数据源对象列表缓存在Redis中的键值。
     */
    public static String makeOnlineFormDatasourceKey(Long formId) {
        return "ONLINE_FORM_DATASOURCE_LIST:" + formId;
    }

    /**
     * 计算在线数据源对象缓存在Redis中的键值。
     *
     * @param datasourceId 在线数据源主键Id。
     * @return 在线数据源对象缓存在Redis中的键值。
     */
    public static String makeOnlineDataSourceKey(Long datasourceId) {
        return "ONLINE_DATASOURCE:" + datasourceId;
    }

    /**
     * 计算在线数据源关联列表对象缓存在Redis中的键值。
     *
     * @param datasourceId 在线数据源主键Id。
     * @return 在线数据源关联列表对象缓存在Redis中的键值。
     */
    public static String makeOnlineDataSourceRelationKey(Long datasourceId) {
        return "ONLINE_DATASOURCE_RELATION:" + datasourceId;
    }

    /**
     * 计算在线字典对象缓存在Redis中的键值。
     *
     * @param dictId 在线字典主键Id。
     * @return 在线字典对象缓存在Redis中的键值。
     */
    public static String makeOnlineDictKey(Long dictId) {
        return "ONLINE_DICT:" + dictId;
    }

    /**
     * 私有构造函数，明确标识该常量类的作用。
     */
    private OnlineRedisKeyUtil() {
    }
}
