package com.bupt.common.online.util;

/**
 * 在线表单自定义脱敏处理器的默认实现类。
 *
 * @author zzh
 * @date 2023-08-10
 */
public class OnlineCustomMaskFieldHandler {

    /**
     * 处理自定义的脱敏数据。可以根据表名和字段名，使用不同的自定义脱敏规则。
     *
     * @param appCode    应用编码。如果不是第三方接入的应用，该值可能为null。
     * @param tableName  在线表单对应的表名。
     * @param columnName 在线表单对应的表字段名
     * @param data       待脱敏的数据。
     * @param maskChar   脱敏掩码字符。
     * @return 脱敏后的数据。
     */
    public String handleMask(String appCode, String tableName, String columnName, String data, char maskChar) {
        throw new UnsupportedOperationException(
                "在运行时抛出该异常，主要为了及时提醒用户提供自己的处理器实现类。请在业务工程中提供该类的具体实现类！");
    }
}
