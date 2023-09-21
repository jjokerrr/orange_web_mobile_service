package com.bupt.common.flow.util;

import org.springframework.stereotype.Component;

/**
 * 工作流自定义扩展工厂类。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Component
public class FlowCustomExtFactory {

    private BaseFlowIdentityExtHelper flowIdentityExtHelper;

    private BaseBusinessDataExtHelper businessDataExtHelper = new BaseBusinessDataExtHelper();

    private BaseOnlineBusinessDataExtHelper onlineBusinessDataExtHelper = new BaseOnlineBusinessDataExtHelper();

    private BaseFlowNotifyExtHelper flowNotifyExtHelper;

    /**
     * 获取业务模块自行实现的用户身份相关的扩展帮助实现类。
     *
     * @return 业务模块自行实现的用户身份相关的扩展帮助实现类。
     */
    public BaseFlowIdentityExtHelper getFlowIdentityExtHelper() {
        return flowIdentityExtHelper;
    }

    /**
     * 注册业务模块自行实现的用户身份扩展帮助实现类。
     *
     * @param helper 业务模块自行实现的用户身份扩展帮助实现类。
     */
    public void registerFlowIdentityExtHelper(BaseFlowIdentityExtHelper helper) {
        this.flowIdentityExtHelper = helper;
    }

    /**
     * 获取有关业务数据的扩展帮助实现类。
     *
     * @return 有关业务数据的扩展帮助实现类。
     */
    public BaseBusinessDataExtHelper getBusinessDataExtHelper() {
        return businessDataExtHelper;
    }
    
    /**
     * 获取有关在线表单业务数据的扩展帮助实现类。
     *
     * @return 有关业务数据的扩展帮助实现类。
     */
    public BaseOnlineBusinessDataExtHelper getOnlineBusinessDataExtHelper() {
        return onlineBusinessDataExtHelper;
    }

    /**
     * 注册流程通知扩展帮助实现类。
     *
     * @param helper 流程通知扩展帮助实现类。
     */
    public void registerNotifyExtHelper(BaseFlowNotifyExtHelper helper) {
        this.flowNotifyExtHelper = helper;
    }

    /**
     * 获取流程通知扩展帮助实现类。
     *
     * @return 流程消息通知扩展帮助实现类。
     */
    public BaseFlowNotifyExtHelper getFlowNotifyExtHelper() {
        if (this.flowNotifyExtHelper == null) {
            this.flowNotifyExtHelper = new BaseFlowNotifyExtHelper();
        }
        return flowNotifyExtHelper;
    }
}
