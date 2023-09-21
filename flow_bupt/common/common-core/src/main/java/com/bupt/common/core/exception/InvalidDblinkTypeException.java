package com.bupt.common.core.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 无效的数据库链接类型自定义异常。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InvalidDblinkTypeException extends RuntimeException {

    /**
     * 构造函数。
     *
     * @param dblinkType 数据库链接类型。
     */
    public InvalidDblinkTypeException(int dblinkType) {
        super("Invalid Dblink Type [" + dblinkType + "].");
    }
}
