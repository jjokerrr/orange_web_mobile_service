package com.bupt.common.online.model.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * 字段类别常量字典对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
public final class FieldKind {

    /**
     * 文件上传字段。
     */
    public static final int UPLOAD = 1;
    /**
     * 图片上传字段。
     */
    public static final int UPLOAD_IMAGE = 2;
    /**
     * 富文本字段。
     */
    public static final int RICH_TEXT = 3;
    /**
     * 字典多选字段。
     */
    public static final int DICT_MULTI_SELECT = 4;
    /**
     * 创建人部门Id。
     */
    public static final int CREATE_DEPT_ID = 19;
    /**
     * 创建时间字段。
     */
    public static final int CREATE_TIME = 20;
    /**
     * 创建人字段。
     */
    public static final int CREATE_USER_ID = 21;
    /**
     * 更新时间字段。
     */
    public static final int UPDATE_TIME = 22;
    /**
     * 更新人字段。
     */
    public static final int UPDATE_USER_ID = 23;
    /**
     * 包含自动编码。
     */
    public static final int AUTO_CODE = 24;
    /**
     * 流程最后审批状态。
     */
    public static final int FLOW_APPROVAL_STATUS = 25;
    /**
     * 流程结束状态。
     */
    public static final int FLOW_FINISHED_STATUS = 26;
    /**
     * 脱敏字段。
     */
    public static final int MASK_FIELD = 27;
    /**
     * 租户过滤字段。
     */
    public static final int TENANT_FILTER = 28;
    /**
     * 逻辑删除字段。
     */
    public static final int LOGIC_DELETE = 31;

    private static final Map<Object, String> DICT_MAP = new HashMap<>(9);
    static {
        DICT_MAP.put(UPLOAD, "文件上传字段");
        DICT_MAP.put(UPLOAD_IMAGE, "图片上传字段");
        DICT_MAP.put(RICH_TEXT, "富文本字段");
        DICT_MAP.put(DICT_MULTI_SELECT, "字典多选字段");
        DICT_MAP.put(CREATE_DEPT_ID, "创建人部门字段");
        DICT_MAP.put(CREATE_TIME, "创建时间字段");
        DICT_MAP.put(CREATE_USER_ID, "创建人字段");
        DICT_MAP.put(UPDATE_TIME, "更新时间字段");
        DICT_MAP.put(UPDATE_USER_ID, "更新人字段");
        DICT_MAP.put(AUTO_CODE, "自动编码字段");
        DICT_MAP.put(FLOW_APPROVAL_STATUS, "流程最后审批状态");
        DICT_MAP.put(FLOW_FINISHED_STATUS, "流程结束状态");
        DICT_MAP.put(MASK_FIELD, "脱敏字段");
        DICT_MAP.put(TENANT_FILTER, "租户过滤字段");
        DICT_MAP.put(LOGIC_DELETE, "逻辑删除字段");
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
    private FieldKind() {
    }
}
