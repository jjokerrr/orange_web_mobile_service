package com.bupt.common.flow.dao;

import com.bupt.common.core.base.dao.BaseDaoMapper;
import com.bupt.common.flow.model.FlowEntryPublishVariable;

import java.util.List;

/**
 * 数据操作访问接口。
 *
 * @author zzh
 * @date 2023-08-10
 */
public interface FlowEntryPublishVariableMapper extends BaseDaoMapper<FlowEntryPublishVariable> {

    /**
     * 批量插入流程发布的变量列表。
     *
     * @param entryPublishVariableList 流程发布的变量列表。
     */
    void insertList(List<FlowEntryPublishVariable> entryPublishVariableList);
}
