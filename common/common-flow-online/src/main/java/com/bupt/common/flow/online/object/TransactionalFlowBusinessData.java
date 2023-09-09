package com.bupt.common.flow.online.object;

import com.bupt.common.core.util.ContextUtil;
import com.bupt.common.online.object.TransactionalBusinessData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.servlet.http.HttpServletRequest;

/**
 * 跨库存储工作流的业务数据的事物性事件数据。
 *
 * @author zzh
 * @date 2023-08-10
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class TransactionalFlowBusinessData extends TransactionalBusinessData {

    /**
     * 流程实例Id。
     */
    private String processInstanceId;
    /**
     * 流程任务Id。
     */
    private String taskId;
    /**
     * 流程任务定义标识。
     */
    private String taskKey;
    /**
     * 流程任务定义名称。
     */
    private String taskName;
    /**
     * 审批注释。
     */
    private String taskComment;

    public TransactionalFlowBusinessData(HttpServletRequest request) {
        super(request);
    }

    /**
     * 获取HttpServletRequest属性中的事物性时间对象。
     *
     * @return 返回设置在当前请求属性中的事务性时间对象。如果不存在则返回NULL。
     */
    public static TransactionalFlowBusinessData getFromRequestAttribute() {
        HttpServletRequest request = ContextUtil.getHttpRequest();
        return (TransactionalFlowBusinessData) request.getAttribute(BUSINESS_DATA_ATTR_KEY);
    }

    /**
     * 获取HttpServletRequest属性中的事物性时间对象。如果存在直接返回，否则创建新对象，并设置到当前请求的指定属性中。
     *
     * @return 返回设置在当前请求属性中的事务性时间对象。
     */
    public static TransactionalFlowBusinessData getOrCreateFromRequestAttribute() {
        HttpServletRequest request = ContextUtil.getHttpRequest();
        TransactionalFlowBusinessData data = (TransactionalFlowBusinessData) request.getAttribute(BUSINESS_DATA_ATTR_KEY);
        if (data != null) {
            return data;
        }
        data = new TransactionalFlowBusinessData(request);
        request.setAttribute(BUSINESS_DATA_ATTR_KEY, data);
        return data;
    }
}
