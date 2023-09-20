package com.bupt.common.flow.online.aop;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.bupt.common.core.object.TokenData;
import com.bupt.common.core.util.AopTargetUtil;
import com.bupt.common.core.util.MyDateUtil;
import com.bupt.common.flow.dao.FlowTransProducerMapper;
import com.bupt.common.flow.model.FlowTaskComment;
import com.bupt.common.flow.model.FlowTransProducer;
import com.bupt.common.flow.online.object.TransactionalFlowBusinessData;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 拦截流程业务多数据库数据写入的AOP对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Aspect
@Component
@Slf4j
public class FlowMultiDatabaseWriteAspect {

    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private FlowTransProducerMapper flowTransProducerMapper;

    @Pointcut("execution(public * com.bupt.common.flow..service.impl..*(..)) " +
            "&& @annotation(com.bupt.common.core.annotation.MultiDatabaseWriteMethod)")
    public void multiDatabaseWriteMethodPointCut() {
        // 空注释，避免sonar警告
    }

    @Around("multiDatabaseWriteMethodPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        String initMethod = AopTargetUtil.getFullMethodName(joinPoint);
        TransactionalFlowBusinessData data = TransactionalFlowBusinessData.getOrCreateFromRequestAttribute();
        if (data.getInitMethod() == null) {
            data.setInitMethod(initMethod);
        }
        // 调用原来的方法
        Object result = joinPoint.proceed();
        if (StrUtil.equals(initMethod, data.getInitMethod()) && CollUtil.isNotEmpty(data.getSqlDataList())) {
            this.fillupTransactionalBusinessData();
            FlowTransProducer producerData = BeanUtil.copyProperties(data, FlowTransProducer.class);
            producerData.setAppCode(TokenData.takeFromRequest().getAppCode());
            producerData.setTryTimes(1);
            String sqlData = JSON.toJSONStringWithDateFormat(
                    data.getSqlDataList(), MyDateUtil.COMMON_SHORT_DATETIME_FORMAT);
            producerData.setSqlData(sqlData);
            flowTransProducerMapper.insert(producerData);
            eventPublisher.publishEvent(data);
        }
        return result;
    }
    
    private void fillupTransactionalBusinessData() {
        FlowTaskComment comment = FlowTaskComment.getFromRequest();
        TransactionalFlowBusinessData data = TransactionalFlowBusinessData.getFromRequestAttribute();
        if (comment != null) {
            data.setProcessInstanceId(comment.getProcessInstanceId());
            data.setTaskId(comment.getTaskId());
            data.setTaskKey(comment.getTaskKey());
            data.setTaskName(comment.getTaskName());
            data.setTaskComment(comment.getTaskComment());
        }
    }
}
