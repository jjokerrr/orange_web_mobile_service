package com.bupt.common.flow.online.aop;

import cn.hutool.core.util.StrUtil;
import com.bupt.common.core.util.AopTargetUtil;
import com.bupt.common.flow.online.object.TransactionalFlowBusinessData;
import com.bupt.common.online.exception.OnlineRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 主要用于拦截FlowTransactionBusinessDataListener监听器类抛出的异常。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Aspect
@Component
@Order(1)
@Slf4j
public class FlowMultiDatabaseWriteExAspect {

    @Pointcut("execution(public * com.bupt.common.flow.online.service.impl..*(..)) " +
            "&& @annotation(com.bupt.common.core.annotation.MultiDatabaseWriteMethod)")
    public void multiDatabaseWriteMethodPointCut() {
        // 空注释，避免sonar警告
    }

    @Around("multiDatabaseWriteMethodPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        String initMethod = AopTargetUtil.getFullMethodName(joinPoint);
        // 调用原来的方法
        Object result = joinPoint.proceed();
        TransactionalFlowBusinessData data = TransactionalFlowBusinessData.getFromRequestAttribute();
        if (StrUtil.equals(initMethod, data.getInitMethod()) && data.getErrorReason() != null) {
            throw new OnlineRuntimeException(data.getErrorReason());
        }
        return result;
    }
}
