package com.bupt.common.core.exception;

/**
 * Redis缓存访问失败。比如：获取分布式数据锁超时、等待线程中断等。
 *
 * @author zzh
 * @date 2023-08-10
 */
public class RedisCacheAccessException  extends RuntimeException {

    /**
     * 构造函数。
     *
     * @param msg   错误信息。
     * @param cause 原始异常。
     */
    public RedisCacheAccessException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
