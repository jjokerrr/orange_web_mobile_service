package com.bupt.common.flow.util;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.bupt.common.flow.base.service.BaseFlowService;
import com.bupt.common.flow.model.FlowWorkOrder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 面向路由表单工作流的业务数据扩展帮助实现类。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Slf4j
public class BaseBusinessDataExtHelper {

    private Map<String, BaseFlowService<?, ?>> serviceMap = MapUtil.newHashMap();

    /**
     * 子类要基于自身所处理的流程定义标识，把子类的this对象，注册到父类的map中。
     *
     * @param processDefinitionKey 流程定义标识。
     * @param service              流程服务实现基类。
     */
    public synchronized void doRegister(String processDefinitionKey, BaseFlowService<?, ?> service) {
        Assert.isTrue(StrUtil.isNotBlank(processDefinitionKey));
        Assert.notNull(service);
        serviceMap.put(processDefinitionKey, service);
    }

    /**
     *
     * 流程结束监听器(FlowFinishedListener) 会在流程结束时调用该方法。
     *
     * @param workOrder 工单对象。
     */
    public void triggerSync(FlowWorkOrder workOrder) {
        BaseFlowService<?, ?> service = serviceMap.get(workOrder.getProcessDefinitionKey());
        if (service != null) {
            try {
                service.syncBusinessData(workOrder);
            } catch (Exception e) {
                String errorMessage = String.format(
                        "Failed to call syncBusinessData with processDefinitionKey {%s}, businessKey {%s}",
                        workOrder.getProcessDefinitionKey(), workOrder.getBusinessKey());
                log.error(errorMessage, e);
                throw e;
            }
        }
    }

    /**
     * 更新主表状态字段的数据。没有业务表的服务实现类，可以自行实现。
     *
     * @param workOrder 工单对象。
     */
    public void updateFlowStatus(FlowWorkOrder workOrder) {
        BaseFlowService<?, ?> service = serviceMap.get(workOrder.getProcessDefinitionKey());
        if (service != null) {
            try {
                service.updateFlowStatus(workOrder);
            } catch (Exception e) {
                String errorMessage = String.format(
                        "Failed to call updateFlowStatus with processDefinitionKey {%s}, businessKey {%s}",
                        workOrder.getProcessDefinitionKey(), workOrder.getBusinessKey());
                log.error(errorMessage, e);
                throw e;
            }
        }
    }

    /**
     * 根据工单对象级联删除业务数据。
     *
     * @param workOrder 工单对象。
     */
    public void deleteBusinessData(FlowWorkOrder workOrder) {
        BaseFlowService<?, ?> service = serviceMap.get(workOrder.getProcessDefinitionKey());
        if (service != null) {
            try {
                service.removeByWorkOrder(workOrder);
            } catch (Exception e) {
                String errorMessage = String.format(
                        "Failed to call deleteBusinessData with processDefinitionKey {%s}, businessKey {%s}",
                        workOrder.getProcessDefinitionKey(), workOrder.getBusinessKey());
                log.error(errorMessage, e);
                throw e;
            }
        }
    }

    /**
     * 获取详细的业务数据，包括主表、一对一、一对多、多对多从表及其字典数据。
     *
     * @param processDefinitionKey 流程定义标识。
     * @param processInstanceId    流程实例Id。
     * @param businessKey          业务主表的主键Id。
     * @return JSON格式化后的业务数据。
     */
    public String getBusinessData(String processDefinitionKey, String processInstanceId, String businessKey) {
        BaseFlowService<?, ?> service = serviceMap.get(processDefinitionKey);
        return service == null ? null : service.getBusinessData(processInstanceId, businessKey);
    }

    /**
     * 格式化参数中的草稿数据，以用于前端显示。包括主表、一对一、一对多、多对多从表及其字典数据。
     *
     * @param processDefinitionKey 流程定义标识。
     * @param processInstanceId    流程实例Id。
     * @param masterData           草稿中的主表数据。
     * @param slaveData            草稿中的全部关联从表数据。
     * @return 格式化后用于前端显示的业务草稿数据。
     */
    public String getNormalizedDraftData(
            String processDefinitionKey, String processInstanceId, JSONObject masterData, JSONObject slaveData) {
        BaseFlowService<?, ?> service = serviceMap.get(processDefinitionKey);
        return service == null ? null : service.getNormalizedDraftData(processInstanceId, masterData, slaveData);
    }
}
