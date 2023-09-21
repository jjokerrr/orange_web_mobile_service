package com.bupt.common.flow.online.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bupt.common.core.config.CoreProperties;
import com.bupt.common.core.object.CallResult;
import com.bupt.common.core.constant.ApplicationConstant;
import com.bupt.common.core.annotation.MyDataSource;
import com.bupt.common.core.annotation.MultiDatabaseWriteMethod;
import com.bupt.common.dbutil.provider.DataSourceProvider;
import com.bupt.common.dbutil.provider.PostgreSqlProvider;
import com.bupt.common.online.util.OnlineDataSourceUtil;
import com.bupt.common.online.config.OnlineProperties;
import com.bupt.common.online.exception.OnlineRuntimeException;
import com.bupt.common.online.model.OnlineDatasource;
import com.bupt.common.online.model.OnlineDatasourceRelation;
import com.bupt.common.online.model.OnlineTable;
import com.bupt.common.online.object.TransactionalBusinessData;
import com.bupt.common.online.service.OnlineDatasourceService;
import com.bupt.common.online.service.OnlineOperationService;
import com.bupt.common.flow.online.object.TransactionalFlowBusinessData;
import com.bupt.common.flow.config.FlowProperties;
import com.bupt.common.flow.constant.FlowConstant;
import com.bupt.common.flow.constant.FlowApprovalType;
import com.bupt.common.flow.constant.FlowTaskStatus;
import com.bupt.common.flow.exception.FlowOperationException;
import com.bupt.common.flow.model.FlowTransProducer;
import com.bupt.common.flow.dao.FlowTransProducerMapper;
import com.bupt.common.flow.model.FlowEntry;
import com.bupt.common.flow.model.FlowWorkOrder;
import com.bupt.common.flow.model.FlowTaskComment;
import com.bupt.common.flow.service.FlowEntryService;
import com.bupt.common.flow.service.FlowApiService;
import com.bupt.common.flow.service.FlowWorkOrderService;
import com.bupt.common.flow.online.service.FlowOnlineOperationService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@MyDataSource(ApplicationConstant.COMMON_FLOW_AND_ONLINE_DATASOURCE_TYPE)
@Service("flowOnlineOperationService")
public class FlowOnlineOperationServiceImpl implements FlowOnlineOperationService {

    @Autowired
    private FlowApiService flowApiService;
    @Autowired
    private FlowWorkOrderService flowWorkOrderService;
    @Autowired
    private FlowEntryService flowEntryService;
    @Autowired
    private OnlineOperationService onlineOperationService;
    @Autowired
    private OnlineDatasourceService onlineDatasourceService;
    @Autowired
    private OnlineProperties onlineProperties;
    @Autowired
    private FlowProperties flowProperties;
    @Autowired
    private CoreProperties coreProperties;
    @Autowired
    private OnlineDataSourceUtil dataSourceUtil;
    @Autowired
    private FlowTransProducerMapper flowTransProducerMapper;

    @MultiDatabaseWriteMethod
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveNewAndStartProcess(
            String processDefinitionId,
            FlowTaskComment flowTaskComment,
            JSONObject taskVariableData,
            OnlineTable table,
            JSONObject data) {
        this.saveNewAndStartProcess(processDefinitionId, flowTaskComment, taskVariableData, table, data, null);
    }

    @MultiDatabaseWriteMethod
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveNewAndStartProcess(
            String processDefinitionId,
            FlowTaskComment flowTaskComment,
            JSONObject taskVariableData,
            OnlineTable masterTable,
            JSONObject masterData,
            Map<OnlineDatasourceRelation, List<JSONObject>> slaveDataListMap) {
        Object dataId = onlineOperationService.saveNewWithRelation(masterTable, masterData, slaveDataListMap);
        Assert.notNull(dataId);
        ProcessInstance instance = flowApiService.start(processDefinitionId, dataId);
        flowWorkOrderService.saveNew(instance, dataId, masterTable.getTableId(), null);
        flowApiService.takeFirstTask(instance.getProcessInstanceId(), flowTaskComment, taskVariableData);
        // 这里需要在创建工单后再次更新一下工单状态，在flowApiService.completeTask中的更新，
        // 因为当时没有创建工单对象，更新会不起任何作用，所以这里要补偿一下。
        Integer approvalStatus = MapUtil.getInt(taskVariableData, FlowConstant.LATEST_APPROVAL_STATUS_KEY);
        flowWorkOrderService.updateLatestApprovalStatusByProcessInstanceId(instance.getId(), approvalStatus);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public FlowWorkOrder saveNewDraftAndStartProcess(
            String processDefinitionId, Long tableId, JSONObject masterData, JSONObject slaveData) {
        ProcessInstance instance = flowApiService.start(processDefinitionId, null);
        return flowWorkOrderService.saveNewWithDraft(
                instance, tableId, null, JSON.toJSONString(masterData), JSON.toJSONString(slaveData));
    }

    @MultiDatabaseWriteMethod
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveNewAndTakeTask(
            String processInstanceId,
            String taskId,
            FlowTaskComment flowTaskComment,
            JSONObject taskVariableData,
            OnlineTable table,
            JSONObject data) {
        this.saveNewAndTakeTask(
                processInstanceId, taskId, flowTaskComment, taskVariableData, table, data, null);
    }

    @MultiDatabaseWriteMethod
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveNewAndTakeTask(
            String processInstanceId,
            String taskId,
            FlowTaskComment flowTaskComment,
            JSONObject taskVariableData,
            OnlineTable masterTable,
            JSONObject masterData,
            Map<OnlineDatasourceRelation, List<JSONObject>> slaveDataListMap) {
        Object dataId = onlineOperationService.saveNewWithRelation(masterTable, masterData, slaveDataListMap);
        Assert.notNull(dataId);
        Task task = flowApiService.getProcessInstanceActiveTask(processInstanceId, taskId);
        flowApiService.setBusinessKeyForProcessInstance(processInstanceId, dataId);
        Map<String, Object> variables =
                flowApiService.initAndGetProcessInstanceVariables(task.getProcessDefinitionId());
        if (taskVariableData == null) {
            taskVariableData = new JSONObject();
        }
        taskVariableData.putAll(variables);
        flowApiService.completeTask(task, flowTaskComment, taskVariableData);
        ProcessInstance instance = flowApiService.getProcessInstance(processInstanceId);
        FlowWorkOrder flowWorkOrder =
                flowWorkOrderService.getFlowWorkOrderByProcessInstanceId(instance.getProcessInstanceId());
        if (flowWorkOrder == null) {
            flowWorkOrderService.saveNew(instance, dataId, masterTable.getTableId(), null);
        } else {
            flowWorkOrder.setBusinessKey(dataId.toString());
            flowWorkOrder.setUpdateTime(new Date());
            flowWorkOrder.setFlowStatus(FlowTaskStatus.SUBMITTED);
            flowWorkOrderService.updateById(flowWorkOrder);
        }
    }

    @MultiDatabaseWriteMethod
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndTakeTask(
            Task task,
            FlowTaskComment flowTaskComment,
            JSONObject taskVariableData,
            OnlineDatasource datasource,
            JSONObject masterData,
            String masterDataId,
            JSONObject slaveData) {
        int flowStatus = FlowTaskStatus.APPROVING;
        if (flowTaskComment.getApprovalType().equals(FlowApprovalType.REFUSE)) {
            flowStatus = FlowTaskStatus.REFUSED;
        } else if (flowTaskComment.getApprovalType().equals(FlowApprovalType.STOP)) {
            flowStatus = FlowTaskStatus.FINISHED;
        }
        OnlineTable masterTable = datasource.getMasterTable();
        Long datasourceId = datasource.getDatasourceId();
        flowWorkOrderService.updateFlowStatusByProcessInstanceId(task.getProcessInstanceId(), flowStatus);
        this.updateMasterData(masterTable, masterData, masterDataId);
        if (slaveData != null) {
            for (Map.Entry<String, Object> relationEntry : slaveData.entrySet()) {
                Long relationId = Long.parseLong(relationEntry.getKey());
                onlineOperationService.updateRelationData(
                        masterTable, masterData, masterDataId, datasourceId, relationId, relationEntry.getValue());
            }
        }
        if (flowTaskComment.getApprovalType().equals(FlowApprovalType.STOP)) {
            Integer s = MapUtil.getInt(taskVariableData, FlowConstant.LATEST_APPROVAL_STATUS_KEY);
            flowWorkOrderService.updateLatestApprovalStatusByProcessInstanceId(task.getProcessInstanceId(), s);
            CallResult stopResult = flowApiService.stopProcessInstance(
                    task.getProcessInstanceId(), flowTaskComment.getTaskComment(), flowStatus);
            if (!stopResult.isSuccess()) {
                throw new FlowOperationException(stopResult.getErrorMessage());
            }
        } else {
            flowApiService.completeTask(task, flowTaskComment, taskVariableData);
        }
    }

    @Override
    public void fixBusinessData(FlowTransProducer flowTransProducer) {
        try {
            String sql = "SELECT * FROM zz_flow_trans_consumer WHERE trans_id = " + flowTransProducer.getTransId();
            List<Map<String, Object>> dataList = dataSourceUtil.query(flowTransProducer.getDblinkId(), sql);
            if (CollUtil.isNotEmpty(dataList)) {
                return;
            }
        } catch (Exception e) {
            log.error("Failed to call dataSourceUtil.query", e);
            throw new OnlineRuntimeException(e.getMessage());
        }
        TransactionalFlowBusinessData businessData = new TransactionalFlowBusinessData();
        businessData.setTransId(flowTransProducer.getTransId());
        businessData.setDblinkId(flowTransProducer.getDblinkId());
        List<TransactionalBusinessData.BusinessSqlData> sqlDataList =
                JSON.parseArray(flowTransProducer.getSqlData(), TransactionalBusinessData.BusinessSqlData.class);
        businessData.setSqlDataList(sqlDataList);
        try {
            this.doHandle(businessData);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new OnlineRuntimeException(e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> calculatePermData(Set<Long> onlineFormEntryIds) {
        if (CollUtil.isEmpty(onlineFormEntryIds)) {
            return new LinkedList<>();
        }
        List<Map<String, Object>> permDataList = new LinkedList<>();
        List<FlowEntry> flowEntries = flowEntryService.getInList(onlineFormEntryIds);
        Set<Long> pageIds = flowEntries.stream().map(FlowEntry::getPageId).collect(Collectors.toSet());
        Map<Long, String> pageAndVariableNameMap =
                onlineDatasourceService.getPageIdAndVariableNameMapByPageIds(pageIds);
        for (FlowEntry flowEntry : flowEntries) {
            JSONObject permData = new JSONObject();
            permData.put("entryId", flowEntry.getEntryId());
            String key = StrUtil.upperFirst(flowEntry.getProcessDefinitionKey());
            List<String> permCodeList = new LinkedList<>();
            String formPermCode = "form" + key;
            permCodeList.add(formPermCode);
            permCodeList.add(formPermCode + ":fragment" + key);
            permData.put("permCodeList", permCodeList);
            String flowUrlPrefix = flowProperties.getUrlPrefix();
            String onlineUrlPrefix = onlineProperties.getUrlPrefix();
            List<String> permList = CollUtil.newLinkedList(
                    onlineUrlPrefix + "/onlineForm/view",
                    onlineUrlPrefix + "/onlineForm/render",
                    onlineUrlPrefix + "/onlineOperation/listByOneToManyRelationId/" + pageAndVariableNameMap.get(flowEntry.getPageId()),
                    flowUrlPrefix + "/flowOperation/viewInitialHistoricTaskInfo",
                    flowUrlPrefix + "/flowOperation/startOnly",
                    flowUrlPrefix + "/flowOperation/viewInitialTaskInfo",
                    flowUrlPrefix + "/flowOperation/viewRuntimeTaskInfo",
                    flowUrlPrefix + "/flowOperation/viewProcessBpmn",
                    flowUrlPrefix + "/flowOperation/viewHighlightFlowData",
                    flowUrlPrefix + "/flowOperation/listFlowTaskComment",
                    flowUrlPrefix + "/flowOperation/cancelWorkOrder",
                    flowUrlPrefix + "/flowOnlineOperation/viewUserTask",
                    flowUrlPrefix + "/flowOnlineOperation/viewHistoricProcessInstance",
                    flowUrlPrefix + "/flowOnlineOperation/submitUserTask",
                    flowUrlPrefix + "/flowOnlineOperation/upload",
                    flowUrlPrefix + "/flowOnlineOperation/download",
                    flowUrlPrefix + "/flowOperation/submitConsign",
                    flowUrlPrefix + "/flowOnlineOperation/startAndTakeUserTask/" + flowEntry.getProcessDefinitionKey(),
                    flowUrlPrefix + "/flowOnlineOperation/startAndSaveDraft/" + flowEntry.getProcessDefinitionKey(),
                    flowUrlPrefix + "/flowOnlineOperation/listWorkOrder/" + flowEntry.getProcessDefinitionKey(),
                    flowUrlPrefix + "/flowOnlineOperation/printWorkOrder/" + flowEntry.getProcessDefinitionKey()
            );
            permData.put("permList", permList);
            permDataList.add(permData);
        }
        return permDataList;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void bulkHandleBusinessData(TransactionalFlowBusinessData businessData) {
        try {
            this.doHandle(businessData);
            // 这里会切换回原有的在线表单表所在的数据库上线文。
            flowTransProducerMapper.deleteById(businessData.getTransId());
        } catch (Exception e) {
            log.error("Failed to commit online business data [** " + JSON.toJSONString(businessData) + " **]", e);
            FlowTransProducer transProducer = flowTransProducerMapper.selectById(businessData.getTransId());
            transProducer.setErrorReason(e.getMessage());
            flowTransProducerMapper.updateById(transProducer);
            businessData.setErrorReason(e.getMessage());
            throw new OnlineRuntimeException(e.getMessage());
        }
    }

    private void doHandle(TransactionalFlowBusinessData businessData) throws Exception {
        Connection conn = null;
        try {
            conn = dataSourceUtil.getConnection(businessData.getDblinkId());
            conn.setAutoCommit(false);
            for (TransactionalBusinessData.BusinessSqlData s : businessData.getSqlDataList()) {
                List<Object> paramList = null;
                if (CollUtil.isNotEmpty(s.getColumnValueList())) {
                    paramList = s.getColumnValueList().stream().map(Object.class::cast).collect(Collectors.toList());
                }
                dataSourceUtil.execute(conn, s.getSql(), paramList);
            }
            this.insertFlowConsumerTrans(conn, businessData);
            conn.commit();
        } catch (Exception e) {
            if (conn != null) {
                conn.rollback();
            }
            log.error(e.getMessage(), e);
            throw new OnlineRuntimeException(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    private void insertFlowConsumerTrans(Connection conn, TransactionalFlowBusinessData businessData) {
        DataSourceProvider provider = dataSourceUtil.getProvider(businessData.getDblinkId());
        Long transId = businessData.getTransId();
        String sql = "INSERT INTO zz_flow_trans_consumer VALUES(?,?)";
        if (provider instanceof PostgreSqlProvider) {
            dataSourceUtil.execute(conn, sql, CollUtil.newArrayList(transId, new java.sql.Date(System.currentTimeMillis())));
        } else {
            dataSourceUtil.execute(conn, sql, CollUtil.newArrayList(transId, new Date()));
        }
    }

    private void updateMasterData(OnlineTable masterTable, JSONObject masterData, String dataId) {
        if (masterData == null) {
            return;
        }
        // 如果存在主表数据，就执行主表数据的更新。
        Map<String, Object> originalMasterData =
                onlineOperationService.getMasterData(masterTable, null, null, dataId);
        for (Map.Entry<String, Object> entry : originalMasterData.entrySet()) {
            masterData.putIfAbsent(entry.getKey(), entry.getValue());
        }
        if (!onlineOperationService.update(masterTable, masterData)) {
            throw new FlowOperationException("主表数据不存在！");
        }
    }
}
