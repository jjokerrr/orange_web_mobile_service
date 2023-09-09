package com.bupt.common.core.annotation;

import com.bupt.common.core.constant.MaskFieldTypeEnum;
import com.bupt.common.core.util.MaskFieldHandler;

import java.lang.annotation.*;

/**
 * 脱敏字段注解。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MaskField {

    /**
     * 脱敏类型。
     *
     * @return 脱敏类型。
     */
    MaskFieldTypeEnum maskType();
    /**
     * 掩码符号。
     *
     * @return 掩码符号。
     */
    char maskChar() default '*';
    /**
     * 前面noMaskPrefix数量的字符不被掩码。
     * 掩码类型为MaskFieldTypeEnum.ID_CARD时可用。
     *
     * @return 从1开始计算，前面不被掩码的字符数。
     */
    int noMaskPrefix() default 1;
    /**
     * 末尾noMaskSuffix数量的字符不被掩码。
     * 掩码类型为MaskFieldTypeEnum.ID_CARD时可用。
     *
     * @return 从1开始计算，末尾不被掩码的字符数。
     */
    int noMaskSuffix() default 1;
    /**
     * 自定义脱敏处理器接口的Class。
     * @return 自定义脱敏处理器接口的Class。
     */
    Class<? extends MaskFieldHandler> handler() default MaskFieldHandler.class;
}
