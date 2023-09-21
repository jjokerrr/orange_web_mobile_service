package com.bupt.common.online.exception;

import com.bupt.common.core.exception.MyRuntimeException;

/**
 * 在线表单运行时异常。
 *
 * @author zzh
 * @date 2023-08-10
 */
public class OnlineRuntimeException extends MyRuntimeException {

    /**
     * 构造函数。
     */
    public OnlineRuntimeException() {

    }

    /**
     * 构造函数。
     *
     * @param msg 错误信息。
     */
    public OnlineRuntimeException(String msg) {
        super(msg);
    }
}
