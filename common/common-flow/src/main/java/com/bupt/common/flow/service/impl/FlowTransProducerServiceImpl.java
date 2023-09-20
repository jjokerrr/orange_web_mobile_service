package com.bupt.common.flow.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bupt.common.core.annotation.MyDataSourceResolver;
import com.bupt.common.core.base.dao.BaseDaoMapper;
import com.bupt.common.core.base.service.BaseService;
import com.bupt.common.core.constant.ApplicationConstant;
import com.bupt.common.core.object.TokenData;
import com.bupt.common.core.util.DefaultDataSourceResolver;
import com.bupt.common.flow.dao.FlowTransProducerMapper;
import com.bupt.common.flow.model.FlowTransProducer;
import com.bupt.common.flow.service.FlowTransProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@MyDataSourceResolver(
        resolver = DefaultDataSourceResolver.class,
        intArg = ApplicationConstant.COMMON_FLOW_AND_ONLINE_DATASOURCE_TYPE)
@Service("flowTransProducerService")
public class FlowTransProducerServiceImpl extends BaseService<FlowTransProducer, Long> implements FlowTransProducerService {

    @Autowired
    private FlowTransProducerMapper flowTransProducerMapper;

    @Override
    protected BaseDaoMapper<FlowTransProducer> mapper() {
        return flowTransProducerMapper;
    }

    @Override
    public List<FlowTransProducer> getListByProcessInstanceIds(Set<String> processInstanceIds) {
        LambdaQueryWrapper<FlowTransProducer> qw = new LambdaQueryWrapper<>();
        qw.in(FlowTransProducer::getProcessInstanceId, processInstanceIds);
        qw.orderByDesc(FlowTransProducer::getTransId);
        return flowTransProducerMapper.selectList(qw);
    }

    @Override
    public FlowTransProducer getByProcessInstanceIdAndTaskId(String processInstanceId, String taskId) {
        LambdaQueryWrapper<FlowTransProducer> qw = new LambdaQueryWrapper<>();
        qw.eq(FlowTransProducer::getProcessInstanceId, processInstanceId);
        qw.eq(FlowTransProducer::getTaskId, taskId);
        String appCode = TokenData.takeFromRequest().getAppCode();
        if (StrUtil.isBlank(appCode)) {
            qw.isNull(FlowTransProducer::getAppCode);
        } else {
            qw.eq(FlowTransProducer::getAppCode, appCode);
        }
        return flowTransProducerMapper.selectOne(qw);
    }

    @Override
    public void deleteByProcessInstanceId(String processInstanceId) {
        LambdaQueryWrapper<FlowTransProducer> qw = new LambdaQueryWrapper<>();
        qw.eq(FlowTransProducer::getProcessInstanceId, processInstanceId);
        String appCode = TokenData.takeFromRequest().getAppCode();
        if (StrUtil.isBlank(appCode)) {
            qw.isNull(FlowTransProducer::getAppCode);
        } else {
            qw.eq(FlowTransProducer::getAppCode, appCode);
        }
        flowTransProducerMapper.delete(qw);
    }
}
