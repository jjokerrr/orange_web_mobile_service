package com.bupt.common.flow.util;

import cn.hutool.core.util.StrUtil;
import com.bupt.common.core.object.TokenData;

/**
 * 工作流 Redis 键生成工具类。
 *
 * @author zzh
 * @date 2023-08-10
 */
public class FlowRedisKeyUtil {

    /**
     * 计算流程对象缓存在Redis中的键值。
     *
     * @param processDefinitionKey 流程标识。
     * @return 流程对象缓存在Redis中的键值。
     */
    public static String makeFlowEntryKey(String processDefinitionKey) {
        String appCode = TokenData.takeFromRequest().getAppCode();
        if (StrUtil.isBlank(appCode)) {
            return "FLOW_ENTRY:" + processDefinitionKey;
        }
        return "FLOW_ENTRY:" + appCode + ":" + processDefinitionKey;
    }

    /**
     * 流程发布对象缓存在Redis中的键值。
     *
     * @param flowEntryPublishId 流程发布主键Id。
     * @return 流程发布对象缓存在Redis中的键值。
     */
    public static String makeFlowEntryPublishKey(Long flowEntryPublishId) {
        return "FLOW_ENTRY_PUBLISH:" + flowEntryPublishId;
    }

    /**
     * 私有构造函数，明确标识该常量类的作用。
     */
    private FlowRedisKeyUtil() {
    }
}
