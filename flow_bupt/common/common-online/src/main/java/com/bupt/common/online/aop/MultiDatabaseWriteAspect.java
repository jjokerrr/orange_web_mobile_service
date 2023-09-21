package com.bupt.common.online.aop;

import cn.hutool.core.util.StrUtil;
import com.bupt.common.core.util.AopTargetUtil;
import com.bupt.common.online.object.TransactionalBusinessData;
import com.bupt.common.online.service.OnlineOperationService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 拦截多数据库数据写入的AOP对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Aspect
@Component
@Slf4j
public class MultiDatabaseWriteAspect {

    @Autowired
    private OnlineOperationService onlineOperationService;

    @Pointcut("execution(public * com.bupt.common.online.service.impl..*(..)) " +
            "&& @annotation(com.bupt.common.core.annotation.MultiDatabaseWriteMethod)")
    public void multiDatabaseAccessMethodPointCut() {
        // 空注释，避免sonar警告
    }

    @Around("multiDatabaseAccessMethodPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        String initMethod = AopTargetUtil.getFullMethodName(joinPoint);
        TransactionalBusinessData data = TransactionalBusinessData.getOrCreateFromRequestAttribute();
        if (data.getInitMethod() == null) {
            data.setInitMethod(initMethod);
        }
        // 调用原来的方法
        Object result = joinPoint.proceed();
        if (StrUtil.equals(initMethod, data.getInitMethod())) {
            onlineOperationService.bulkHandleBusinessData(data);
        }
        return result;
    }
}
