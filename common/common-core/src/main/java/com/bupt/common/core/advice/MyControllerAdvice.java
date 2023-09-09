package com.bupt.common.core.advice;

import com.bupt.common.core.util.MyDateUtil;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Controller的环绕拦截类。
 *
 * @author zzh
 * @date 2023-08-10
 */
@ControllerAdvice
public class MyControllerAdvice {

    /**
     * 转换前端传入的日期变量参数为指定格式。
     *
     * @param binder 数据绑定参数。
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Date.class,
                new CustomDateEditor(new SimpleDateFormat(MyDateUtil.COMMON_SHORT_DATETIME_FORMAT), false));
    }
}
