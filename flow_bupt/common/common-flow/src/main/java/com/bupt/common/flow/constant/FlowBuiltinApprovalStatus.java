package com.bupt.common.flow.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * 内置的流程审批状态。
 *
 * @author zzh
 * @date 2023-08-10
 */
public class FlowBuiltinApprovalStatus {

    /**
     * 同意。
     */
    public static final int AGREED = 1;
    /**
     * 拒绝。
     */
    public static final int REFUSED = 2;
    /**
     * 会签同意。
     */
    public static final int MULTI_AGREED = 3;
    /**
     * 会签拒绝。
     */
    public static final int MULTI_REFUSED = 4;

    private static final Map<Object, String> DICT_MAP = new HashMap<>(2);
    static {
        DICT_MAP.put(AGREED, "同意");
        DICT_MAP.put(REFUSED, "拒绝");
        DICT_MAP.put(MULTI_AGREED, "会签同意");
        DICT_MAP.put(MULTI_REFUSED, "会签拒绝");
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
    private FlowBuiltinApprovalStatus() {
    }
}
