package com.bupt.common.flow.base.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.ReflectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bupt.common.core.base.service.BaseService;
import com.bupt.common.core.exception.MyRuntimeException;
import com.bupt.common.core.object.CallResult;
import com.bupt.common.core.object.MyRelationParam;
import com.bupt.common.core.util.MyDateUtil;
import com.bupt.common.core.util.MyModelUtil;
import com.bupt.common.flow.constant.FlowApprovalType;
import com.bupt.common.flow.constant.FlowConstant;
import com.bupt.common.flow.constant.FlowTaskStatus;
import com.bupt.common.flow.exception.FlowOperationException;
import com.bupt.common.flow.model.FlowTaskComment;
import com.bupt.common.flow.model.FlowWorkOrder;
import com.bupt.common.flow.model.FlowWorkOrderExt;
import com.bupt.common.flow.service.FlowApiService;
import com.bupt.common.flow.service.FlowWorkOrderService;
import com.bupt.common.flow.vo.FlowWorkOrderVo;
import com.bupt.common.redis.cache.SessionCacheHelper;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 路由表单工作流服务抽象类。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Slf4j
public abstract class BaseFlowService<M, K extends Serializable> extends BaseService<M, K> {

    @Autowired
    private FlowApiService flowApiService;
    @Autowired
    private FlowWorkOrderService flowWorkOrderService;
    @Autowired
    private SessionCacheHelper cacheHelper;

    @Transactional(rollbackFor = Exception.class)
    public void startWithBusinessKey(String processDefinitionId, K dataId) {
        ProcessInstance instance = flowApiService.start(processDefinitionId, dataId);
        flowWorkOrderService.saveNew(instance, dataId, null, super.tableName);
    }

    @Transactional(rollbackFor = Exception.class)
    public void startAndTakeFirst(
            String processDefinitionId, K dataId, FlowTaskComment comment, JSONObject variables) {
        ProcessInstance instance = flowApiService.start(processDefinitionId, dataId);
        flowWorkOrderService.saveNew(instance, dataId, null, super.tableName);
        flowApiService.takeFirstTask(instance.getProcessInstanceId(), comment, variables);
        // 这里需要在创建工单后再次更新一下工单状态，在flowApiService.completeTask中的更新，
        // 因为当时没有创建工单对象，更新会不起任何作用，所以这里要补偿一下。
        Integer approvalStatus = MapUtil.getInt(variables, FlowConstant.LATEST_APPROVAL_STATUS_KEY);
        flowWorkOrderService.updateLatestApprovalStatusByProcessInstanceId(instance.getId(), approvalStatus);
    }

    @Transactional(rollbackFor = Exception.class)
    public FlowWorkOrder saveNewDraftAndStartProcess(String processDefinitionId, String masterData, String slaveData) {
        ProcessInstance instance = flowApiService.start(processDefinitionId, null);
        Map<String, Object> variableMap = flowApiService.initAndGetProcessInstanceVariables(processDefinitionId);
        flowApiService.setProcessInstanceVariables(instance.getProcessInstanceId(), variableMap);
        return flowWorkOrderService.saveNewWithDraft(instance, null, super.tableName, masterData, slaveData);
    }

    @Transactional(rollbackFor = Exception.class)
    public void takeFirstTask(
            String processInstanceId, String taskId, K dataId, FlowTaskComment comment, JSONObject variables) {
        Task task = flowApiService.getProcessInstanceActiveTask(processInstanceId, taskId);
        flowApiService.setBusinessKeyForProcessInstance(processInstanceId, dataId);
        ProcessInstance instance = flowApiService.getProcessInstance(processInstanceId);
        FlowWorkOrder flowWorkOrder =
                flowWorkOrderService.getFlowWorkOrderByProcessInstanceId(instance.getProcessInstanceId());
        if (flowWorkOrder == null) {
            flowWorkOrderService.saveNew(instance, dataId, null, super.tableName);
        } else {
            flowWorkOrder.setBusinessKey(dataId.toString());
            flowWorkOrder.setUpdateTime(new Date());
            flowWorkOrder.setFlowStatus(FlowTaskStatus.SUBMITTED);
            flowWorkOrderService.updateById(flowWorkOrder);
        }
        flowApiService.completeTask(task, comment, variables);
    }

    @Transactional(rollbackFor = Exception.class)
    public void takeTask(Task task, K dataId, FlowTaskComment comment, JSONObject variables) {
        int flowStatus = FlowTaskStatus.APPROVING;
        if (comment.getApprovalType().equals(FlowApprovalType.REFUSE)) {
            flowStatus = FlowTaskStatus.REFUSED;
        } else if (comment.getApprovalType().equals(FlowApprovalType.STOP)) {
            flowStatus = FlowTaskStatus.FINISHED;
        }
        if (comment.getApprovalType().equals(FlowApprovalType.STOP)) {
            Integer s = MapUtil.getInt(variables, FlowConstant.LATEST_APPROVAL_STATUS_KEY);
            flowWorkOrderService.updateLatestApprovalStatusByProcessInstanceId(task.getProcessInstanceId(), s);
            CallResult stopResult = flowApiService.stopProcessInstance(
                    task.getProcessInstanceId(), comment.getTaskComment(), flowStatus);
            if (!stopResult.isSuccess()) {
                throw new FlowOperationException(stopResult.getErrorMessage());
            }
        } else {
            flowWorkOrderService.updateFlowStatusByProcessInstanceId(task.getProcessInstanceId(), flowStatus);
            flowApiService.completeTask(task, comment, variables);
        }
    }

    public void buildDraftData(List<FlowWorkOrderVo> draftWorkOrderList) {
        if (CollUtil.isEmpty(draftWorkOrderList)) {
            return;
        }
        Set<Long> workOrderIdSet = draftWorkOrderList.stream()
                .map(FlowWorkOrderVo::getWorkOrderId).collect(Collectors.toSet());
        Map<Long, FlowWorkOrderExt> workOrderExtMap =
                flowWorkOrderService.getFlowWorkOrderExtByWorkOrderIds(workOrderIdSet)
                        .stream().collect(Collectors.toMap(FlowWorkOrderExt::getWorkOrderId, c -> c));
        for (FlowWorkOrderVo workOrder : draftWorkOrderList) {
            FlowWorkOrderExt workOrderExt = workOrderExtMap.get(workOrder.getWorkOrderId());
            if (workOrderExt == null) {
                continue;
            }
            JSONObject draftData = JSON.parseObject(workOrderExt.getDraftData());
            JSONObject masterData = draftData.getJSONObject(FlowConstant.MASTER_DATA_KEY);
            JSONObject slaveData = draftData.getJSONObject(FlowConstant.SLAVE_DATA_KEY);
            M model;
            if (masterData != null) {
                model = masterData.toJavaObject(modelClass);
                super.buildRelationForData(model, MyRelationParam.dictOnly());
            } else {
                model = ReflectUtil.newInstance(modelClass);
            }
            this.bindLocalOneToOneSlaveData(model, slaveData);
            super.buildLocalOneToOneDictOnly(model);
            workOrder.setMasterData(BeanUtil.beanToMap(model));
        }
    }

    /**
     * 在流程实例审批结束后，需要进行审批表到发布表数据同步的服务实现子类，需要实现该方法。
     *
     * @param workOrder 工单对象。
     */
    public void syncBusinessData(FlowWorkOrder workOrder) {
        // 请自行在子类中实现当前service的数据同步逻辑。
    }

    /**
     * 在流程实例审批结束后，需要进行业务表的状态更新，业务Service需要实现该方法。
     *
     * @param workOrder 工单对象。
     */
    public void updateFlowStatus(FlowWorkOrder workOrder) {
        if (flowStatusField == null && flowLatestApprovalStatusField == null) {
            return;
        }
        Serializable id = this.convertToKeyValue(workOrder.getBusinessKey());
        M data = this.getById(id);
        if (data == null) {
            String msg = StrFormatter.format(
                    "WorkOrderId [{}] don't find business data by key [{}] while calling [updateFlowStatus].",
                    workOrder.getWorkOrderId(), workOrder.getBusinessKey());
            log.warn(msg);
            return;
        }
        if (flowStatusField != null) {
            ReflectUtil.setFieldValue(data, flowStatusField, workOrder.getFlowStatus());
        }
        if (flowLatestApprovalStatusField != null) {
            ReflectUtil.setFieldValue(data, flowLatestApprovalStatusField, workOrder.getLatestApprovalStatus());
        }
        mapper().updateById(data);
    }

    /**
     * 根据工单对象删除流程业务数据。
     *
     * @param workOrder 工单对象。
     */
    @SuppressWarnings("unchecked")
    public void removeByWorkOrder(FlowWorkOrder workOrder) {
        Serializable id = this.convertToKeyValue(workOrder.getBusinessKey());
        M data = this.getById(id);
        if (data == null) {
            String msg = StrFormatter.format(
                    "WorkOrderId [{}] don't find business data by key [{}] while calling [removeByWorkOrder].",
                    workOrder.getWorkOrderId(), workOrder.getBusinessKey());
            log.warn(msg);
            return;
        }
        // 级联删除业务数据，调用的是橙单默认生成的remove方法，参数类型为主键类型，如Long、String等。
        // 进一步补充说明，橙单默认生成的remove方法，会根据在生成器中的配置，生成关联从表数据的级联删除代码。
        this.remove((K) id);
    }

    /**
     * 获取业务详情数据。
     *
     * @param processInstanceId 流程实例Id。
     * @param businessKey       业务主键Id。如果与实际主键值类型不同，需要在子类中自行完成类型转换。
     * @return 业务主表数据，以及关联从表数据。
     */
    @SuppressWarnings("unchecked")
    public String getBusinessData(String processInstanceId, String businessKey) {
        Serializable id = this.convertToKeyValue(businessKey);
        M data = this.getByIdWithRelation((K) id, MyRelationParam.full());
        return JSON.toJSONStringWithDateFormat(data, MyDateUtil.COMMON_SHORT_DATETIME_FORMAT);
    }

    /**
     * 规格化工单扩展表中保存的草稿数据。如果仅仅包含主表数据，可以使用当前的缺省实现。
     *
     * @param processInstanceId 流程实例Id。
     * @param masterData        草稿中的主表数据。
     * @param slaveData         草稿中的全部关联从表数据。
     * @return 格式化后用于前端显示的业务草稿数据。
     */
    public String getNormalizedDraftData(String processInstanceId, JSONObject masterData, JSONObject slaveData) {
        // 看到这个异常千万别慌，这是及时的提示您，如果草稿中包含了关联从表数据，就一定要在业务的ServiceImpl中实现该方法。
        // 在子类中实现主表和关联从表数据的数据组装。通用的方法中，只能解析主表数据，关联从表各式各样，所以需要在子类中实现。
        if (slaveData != null) {
            throw new UnsupportedOperationException(
                    "Please implement getNormalizedDraftData by self, because draft data includes related slaveData");
        }
        if (masterData == null) {
            return JSON.toJSONString(ReflectUtil.newInstance(modelClass));
        }
        M model = BeanUtil.toBean(masterData, modelClass);
        // 这里会缺省缓存主表中上传字段的下载地址，以便在显示草稿数据时，这些上传字段对应的download方法，不会被数据权限过滤。
        Set<String> cachedDownableFilename =
                new HashSet<>(MyModelUtil.extractDownloadFileName(model, modelClass));
        cacheHelper.putSessionDownloadableFileNameSet(cachedDownableFilename);
        return JSON.toJSONStringWithDateFormat(model, MyDateUtil.COMMON_SHORT_DATETIME_FORMAT);
    }

    private Serializable convertToKeyValue(String businessKey) {
        if (idFieldClass.equals(String.class)) {
            return businessKey;
        } else if (idFieldClass.equals(Long.class)) {
            return Long.valueOf(businessKey);
        } else if (idFieldClass.equals(Integer.class)) {
            return Integer.valueOf(businessKey);
        } else {
            throw new MyRuntimeException("Unsupported Primary Key Field Type.");
        }
    }

    private void bindLocalOneToOneSlaveData(M model, JSONObject slaveData) {
        if (slaveData == null) {
            return;
        }
        for (BaseService.RelationStruct relationStruct : super.localRelationOneToOneStructList) {
            Field relationField = relationStruct.getRelationField();
            JSONObject slaveObject = slaveData.getJSONObject(relationField.getName());
            if (slaveObject != null) {
                ReflectUtil.setFieldValue(model, relationField,
                        slaveObject.toJavaObject(relationStruct.getRelationOneToOne().slaveModelClass()));
            }
        }
    }
}
