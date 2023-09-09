package com.bupt.common.flow.service.impl;

import com.bupt.common.core.annotation.MyDataSourceResolver;
import com.bupt.common.core.constant.ApplicationConstant;
import com.bupt.common.core.util.DefaultDataSourceResolver;
import com.bupt.common.flow.dao.FlowVariableDisplayMapper;
import com.bupt.common.flow.model.FlowVariableDisplay;
import com.bupt.common.flow.service.FlowVariableDisplayService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@MyDataSourceResolver(
        resolver = DefaultDataSourceResolver.class,
        intArg = ApplicationConstant.COMMON_FLOW_AND_ONLINE_DATASOURCE_TYPE)
@Service("FlowVariableDisplayService")
public class FlowVariableDisplayServiceImpl implements FlowVariableDisplayService {


    @Autowired
    private FlowVariableDisplayMapper flowVariableDisplayMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public FlowVariableDisplay saveNew(ProcessInstance instance, Object dataId, Long onlineTableId, String tableName){
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public FlowVariableDisplay saveNewWithDraft(
            ProcessInstance instance, Long onlineTableId, String tableName, String masterData, String slaveData){
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean remove(List<String> taskKeyList) {
        for(int i = 0;i < taskKeyList.size();i++){
            System.out.println(taskKeyList.get(i));
            flowVariableDisplayMapper.delete(taskKeyList.get(i));
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean removeByEntry(String EntryId) {
        flowVariableDisplayMapper.deleteByEntry(EntryId);
        return true;
    }


    @Override
    public boolean removeByTaskKey(String taskKey) {
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateByEntry(String EntryId, List<FlowVariableDisplay> flowVaribleDisplayList) {
        if(removeByEntry(EntryId)){
            for(int i = 0; i < flowVaribleDisplayList.size(); i++){
                flowVariableDisplayMapper.add(flowVaribleDisplayList.get(i));
            }
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<FlowVariableDisplay> select(String taskKey) {
        return flowVariableDisplayMapper.select(taskKey);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<FlowVariableDisplay> selectByEntry(String entryId) {
        return flowVariableDisplayMapper.selectByEntry(entryId);
    }
}
