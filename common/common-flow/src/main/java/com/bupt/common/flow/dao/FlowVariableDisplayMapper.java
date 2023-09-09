package com.bupt.common.flow.dao;

import com.bupt.common.core.base.dao.BaseDaoMapper;
import com.bupt.common.flow.model.FlowVariableDisplay;

import java.util.List;

/**
 * FlowVaribleDisplayDto数据操作访问接口。
 *
 * @author zzh
 * @date 2023-08-10
 */
public interface FlowVariableDisplayMapper extends BaseDaoMapper<FlowVariableDisplay> {

    void add(FlowVariableDisplay flowVariableDisplay);

    void delete(String taskKey);

    void deleteByEntry(String entryId);

    List<FlowVariableDisplay> select(String taskKey);

    List<FlowVariableDisplay> selectByEntry(String entryId);

}
