package com.bupt.common.mobile.model.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * 移动端入口常量字典对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
public final class MobileEntryType {

    /**
     * 轮播图。
     */
    public static final int BANNER = 0;
    /**
     * 九宫格。
     */
    public static final int SQUARE = 1;
    /**
     * 九宫格分组
     */
    public static final int SQUARE_GROUP = 2;

    private static final Map<Object, String> DICT_MAP = new HashMap<>(2);
    static {
        DICT_MAP.put(BANNER, "轮播图");
        DICT_MAP.put(SQUARE, "九宫格");
        DICT_MAP.put(SQUARE_GROUP, "九宫格分组");
    }

    /**
     * 判断参数是否为当前常量字典的合法值。
     *
     * @param value 待验证的参数值。
     * @return 合法返回true，否则false。
     */
    public static boolean isValid(Integer value) {
        return value != null && DICT_MAP.containsKey(value);
    }

    /**
     * 私有构造函数，明确标识该常量类的作用。
     */
    private MobileEntryType() {
    }
}
