package com.bupt.common.flow.dao;

import com.bupt.common.core.base.dao.BaseDaoMapper;
import com.bupt.common.flow.model.FlowMessageCandidateIdentity;
import org.apache.ibatis.annotations.Param;

/**
 * 流程任务消息的候选身份数据操作访问接口。
 *
 * @author zzh
 * @date 2023-08-10
 */
public interface FlowMessageCandidateIdentityMapper extends BaseDaoMapper<FlowMessageCandidateIdentity> {

    /**
     * 删除指定流程实例的消息关联数据。
     *
     * @param processInstanceId 流程实例Id。
     */
    void deleteByProcessInstanceId(@Param("processInstanceId") String processInstanceId);
}
