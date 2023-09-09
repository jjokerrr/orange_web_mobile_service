package com.bupt.common.flow.online.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.bupt.common.core.annotation.MyDataSource;
import com.bupt.common.core.constant.ApplicationConstant;
import com.bupt.common.flow.base.service.BaseFlowOnlineService;
import com.bupt.common.flow.model.FlowWorkOrder;
import com.bupt.common.flow.util.FlowCustomExtFactory;
import com.bupt.common.flow.online.object.TransactionalFlowBusinessData;
import com.bupt.common.online.exception.OnlineRuntimeException;
import com.bupt.common.online.model.OnlineColumn;
import com.bupt.common.online.model.OnlineTable;
import com.bupt.common.online.model.OnlineDatasource;
import com.bupt.common.online.model.OnlineDatasourceRelation;
import com.bupt.common.online.model.constant.FieldKind;
import com.bupt.common.online.service.OnlineDatasourceRelationService;
import com.bupt.common.online.service.OnlineDatasourceService;
import com.bupt.common.online.service.OnlineOperationService;
import com.bupt.common.online.service.OnlineTableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 在线表单和流程监听器进行数据对接时的服务实现类。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Slf4j
@MyDataSource(ApplicationConstant.COMMON_FLOW_AND_ONLINE_DATASOURCE_TYPE)
@Service("flowOnlineBusinessService")
public class FlowOnlineBusinessServiceImpl implements BaseFlowOnlineService {

    @Autowired
    private FlowCustomExtFactory flowCustomExtFactory;
    @Autowired
    private OnlineTableService onlineTableService;
    @Autowired
    private OnlineDatasourceService onlineDatasourceService;
    @Autowired
    private OnlineDatasourceRelationService onlineDatasourceRelationService;
    @Autowired
    private OnlineOperationService onlineOperationService;

    @PostConstruct
    public void doRegister() {
        flowCustomExtFactory.getOnlineBusinessDataExtHelper().setOnlineBusinessService(this);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateFlowStatus(FlowWorkOrder workOrder) {
        OnlineTable onlineTable = onlineTableService.getOnlineTableFromCache(workOrder.getOnlineTableId());
        if (onlineTable == null) {
            log.error("OnlineTableId [{}] doesn't exist while calling FlowOnlineBusinessServiceImpl.updateFlowStatus",
                    workOrder.getOnlineTableId());
            return;
        }
        String dataId = workOrder.getBusinessKey();
        this.handleTransactionalFlowBusinessData(workOrder, "FlowOnlineBusinessServiceImpl.updateFlowStatus");
        for (OnlineColumn column : onlineTable.getColumnMap().values()) {
            if (ObjectUtil.equals(column.getFieldKind(), FieldKind.FLOW_FINISHED_STATUS)) {
                onlineOperationService.updateColumn(onlineTable, dataId, column, workOrder.getFlowStatus());
            }
            if (ObjectUtil.equals(column.getFieldKind(), FieldKind.FLOW_APPROVAL_STATUS)) {
                onlineOperationService.updateColumn(onlineTable, dataId, column, workOrder.getLatestApprovalStatus());
            }
        }
    }

    @Override
    public void deleteBusinessData(FlowWorkOrder workOrder) {
        OnlineTable onlineTable = onlineTableService.getOnlineTableFromCache(workOrder.getOnlineTableId());
        if (onlineTable == null) {
            log.error("OnlineTableId [{}] doesn't exist while calling FlowOnlineBusinessServiceImpl.deleteBusinessData",
                    workOrder.getOnlineTableId());
            return;
        }
        OnlineDatasource datasource =
                onlineDatasourceService.getOnlineDatasourceByMasterTableId(onlineTable.getTableId());
        List<OnlineDatasourceRelation> relationList =
                onlineDatasourceRelationService.getOnlineDatasourceRelationListFromCache(CollUtil.newHashSet(datasource.getDatasourceId()));
        String dataId = workOrder.getBusinessKey();
        for (OnlineDatasourceRelation relation : relationList) {
            OnlineTable slaveTable = onlineTableService.getOnlineTableFromCache(relation.getSlaveTableId());
            if (slaveTable == null) {
                throw new OnlineRuntimeException("数据验证失败，数据源关联 [" + relation.getRelationName() + "] 的从表Id不存在！");
            }
            relation.setSlaveTable(slaveTable);
        }
        this.handleTransactionalFlowBusinessData(workOrder, "FlowOnlineBusinessServiceImpl.deleteBusinessData");
        onlineOperationService.delete(onlineTable, relationList, dataId);
    }

    private void handleTransactionalFlowBusinessData(FlowWorkOrder workOrder, String desc) {
        TransactionalFlowBusinessData eventData = TransactionalFlowBusinessData.getFromRequestAttribute();
        if (eventData != null) {
            eventData.setProcessInstanceId(workOrder.getProcessInstanceId());
            eventData.setOperationDesc(desc);
        }
    }
}
