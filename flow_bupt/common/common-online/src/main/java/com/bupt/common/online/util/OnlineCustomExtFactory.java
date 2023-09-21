package com.bupt.common.online.util;

import org.springframework.stereotype.Component;

/**
 * 在线表单自定义扩展工厂类。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Component
public class OnlineCustomExtFactory {

    private OnlineCustomMaskFieldHandler customMaskFieldHandler = new OnlineCustomMaskFieldHandler();

    /**
     * 设置自定义脱敏规则处理器对象。推荐设置的对象为Bean对象，并在服务启动过程中完成自动注册，运行时直接使用即可。
     *
     * @param customMaskFieldHandler 自定义脱敏规则处理器对象。
     */
    public void setCustomMaskFieldHandler(OnlineCustomMaskFieldHandler customMaskFieldHandler) {
        this.customMaskFieldHandler = customMaskFieldHandler;
    }

    /**
     * 返回在线表单的自定义脱敏规则处理器对象。该Bean对象需要在业务代码中实现自行实现。
     *
     * @return 在线表单的自定义脱敏规则处理器对象。
     */
    public OnlineCustomMaskFieldHandler getCustomMaskFieldHandler() {
        return customMaskFieldHandler;
    }
}
