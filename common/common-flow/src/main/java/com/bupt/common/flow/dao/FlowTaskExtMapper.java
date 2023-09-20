package com.bupt.common.flow.dao;

import com.bupt.common.core.base.dao.BaseDaoMapper;
import com.bupt.common.flow.model.FlowTaskExt;

import java.util.List;

/**
 * 流程任务扩展数据操作访问接口。
 *
 * @author zzh
 * @date 2023-08-10
 */
public interface FlowTaskExtMapper extends BaseDaoMapper<FlowTaskExt> {

    /**
     * 批量插入流程任务扩展信息列表。
     *
     * @param flowTaskExtList 流程任务扩展信息列表。
     */
    void insertList(List<FlowTaskExt> flowTaskExtList);
}
