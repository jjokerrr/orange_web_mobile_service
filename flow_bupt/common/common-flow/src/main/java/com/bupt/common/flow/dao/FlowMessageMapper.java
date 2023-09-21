package com.bupt.common.flow.dao;

import com.bupt.common.core.base.dao.BaseDaoMapper;
import com.bupt.common.flow.model.FlowMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 工作流消息数据操作访问接口。
 *
 * @author zzh
 * @date 2023-08-10
 */
public interface FlowMessageMapper extends BaseDaoMapper<FlowMessage> {

    /**
     * 获取指定用户和身份分组Id集合的催办消息列表。
     *
     * @param tenantId   租户Id。
     * @param appCode    应用编码。
     * @param loginName  用户的登录名。与流程任务的assignee精确匹配。
     * @param groupIdSet 用户身份分组Id集合。
     * @return 查询后的催办消息列表。
     */
    List<FlowMessage> getRemindingMessageListByUser(
            @Param("tenantId") Long tenantId,
            @Param("appCode") String appCode,
            @Param("loginName") String loginName,
            @Param("groupIdSet") Set<String> groupIdSet);

    /**
     * 获取指定用户和身份分组Id集合的抄送消息列表。
     *
     * @param tenantId   租户Id。
     * @param appCode    应用编码。
     * @param loginName  用户登录名。
     * @param groupIdSet 用户身份分组Id集合。
     * @param read       true表示已读，false表示未读。
     * @return 查询后的抄送消息列表。
     */
    List<FlowMessage> getCopyMessageListByUser(
            @Param("tenantId") Long tenantId,
            @Param("appCode") String appCode,
            @Param("loginName") String loginName,
            @Param("groupIdSet") Set<String> groupIdSet,
            @Param("read") Boolean read);

    /**
     * 计算当前用户催办消息的数量。
     *
     * @param tenantId   租户Id。
     * @param appCode    应用编码。
     * @param loginName  用户登录名。
     * @param groupIdSet 用户身份分组Id集合。
     * @return 数据数量。
     */
    int countRemindingMessageListByUser(
            @Param("tenantId") Long tenantId,
            @Param("appCode") String appCode,
            @Param("loginName") String loginName,
            @Param("groupIdSet") Set<String> groupIdSet);

    /**
     * 计算当前用户未读抄送消息的数量。
     *
     * @param tenantId   租户Id。
     * @param appCode    应用编码。
     * @param loginName  用户登录名。
     * @param groupIdSet 用户身份分组Id集合。
     * @return 数据数量
     */
    int countCopyMessageListByUser(
            @Param("tenantId") Long tenantId,
            @Param("appCode") String appCode,
            @Param("loginName") String loginName,
            @Param("groupIdSet") Set<String> groupIdSet);
}
