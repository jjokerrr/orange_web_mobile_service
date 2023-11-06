package com.bupt.common.flow.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bupt.common.flow.dto.FlowVaribleDisplayDto;
import com.github.pagehelper.Page;
import com.bupt.common.core.annotation.MyDataSourceResolver;
import com.bupt.common.core.base.dao.BaseDaoMapper;
import com.bupt.common.core.base.service.BaseService;
import com.bupt.common.core.constant.ApplicationConstant;
import com.bupt.common.core.object.CallResult;
import com.bupt.common.core.object.MyRelationParam;
import com.bupt.common.core.object.TokenData;
import com.bupt.common.core.util.MyModelUtil;
import com.bupt.common.core.util.DefaultDataSourceResolver;
import com.bupt.common.redis.util.CommonRedisUtil;
import com.bupt.common.sequence.wrapper.IdGeneratorWrapper;
import com.bupt.common.flow.listener.*;
import com.bupt.common.flow.object.*;
import com.bupt.common.flow.util.FlowRedisKeyUtil;
import com.bupt.common.flow.util.BaseFlowIdentityExtHelper;
import com.bupt.common.flow.util.FlowCustomExtFactory;
import com.bupt.common.flow.constant.FlowConstant;
import com.bupt.common.flow.dao.*;
import com.bupt.common.flow.model.*;
import com.bupt.common.flow.service.*;
import com.bupt.common.flow.model.constant.FlowEntryStatus;
import com.bupt.common.flow.model.constant.FlowVariableType;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.*;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@MyDataSourceResolver(
        resolver = DefaultDataSourceResolver.class,
        intArg = ApplicationConstant.COMMON_FLOW_AND_ONLINE_DATASOURCE_TYPE)
@Service("flowEntryService")
public class FlowEntryServiceImpl extends BaseService<FlowEntry, Long> implements FlowEntryService {

    @Autowired
    private FlowEntryMapper flowEntryMapper;

    @Autowired
    private FlowVariableDisplayMapper flowVariableDisplayMapper;
    @Autowired
    private FlowEntryPublishMapper flowEntryPublishMapper;
    @Autowired
    private FlowEntryPublishVariableMapper flowEntryPublishVariableMapper;
    @Autowired
    private FlowEntryVariableService flowEntryVariableService;
    @Autowired
    private FlowCategoryService flowCategoryService;
    @Autowired
    private FlowTaskExtService flowTaskExtService;
    @Autowired
    private FlowApiService flowApiService;
    @Autowired
    private FlowCustomExtFactory flowCustomExtFactory;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private IdGeneratorWrapper idGenerator;
    @Autowired
    private CommonRedisUtil commonRedisUtil;

    private static final Integer FLOW_ENTRY_PUBLISH_TTL = 60 * 60 * 24;

    /**
     * 返回当前Service的主表Mapper对象。
     *
     * @return 主表Mapper对象。
     */
    @Override
    protected BaseDaoMapper<FlowEntry> mapper() {
        return flowEntryMapper;
    }

//    @Override
//    protected BaseDaoMapper<FlowVaribleDisplayDto> mapperVaribleDisplay() { return flowVariableDisplayMapper; }



    @Transactional(rollbackFor = Exception.class)
    @Override
    public FlowEntry saveNew(FlowEntry flowEntry) {
        flowEntry.setEntryId(idGenerator.nextLongId());
        flowEntry.setStatus(FlowEntryStatus.UNPUBLISHED);
        TokenData tokenData = TokenData.takeFromRequest();
        flowEntry.setAppCode(tokenData.getAppCode());
        flowEntry.setTenantId(tokenData.getTenantId());
        flowEntry.setUpdateUserId(tokenData.getUserId());
        flowEntry.setCreateUserId(tokenData.getUserId());
        Date now = new Date();
        flowEntry.setUpdateTime(now);
        flowEntry.setCreateTime(now);
        flowEntryMapper.insert(flowEntry);
        this.insertBuiltinEntryVariables(flowEntry.getEntryId());
        return flowEntry;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void publish(FlowEntry flowEntry, String initTaskInfo) throws XMLStreamException {
        commonRedisUtil.evictFormCache(
                FlowRedisKeyUtil.makeFlowEntryKey(flowEntry.getProcessDefinitionKey()));
        FlowCategory flowCategory = flowCategoryService.getById(flowEntry.getCategoryId());
        InputStream xmlStream = new ByteArrayInputStream(
                flowEntry.getBpmnXml().getBytes(StandardCharsets.UTF_8));
        @Cleanup XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(xmlStream);
        BpmnXMLConverter converter = new BpmnXMLConverter();
        BpmnModel bpmnModel = converter.convertToBpmnModel(reader);
        bpmnModel.getMainProcess().setName(flowEntry.getProcessDefinitionName());
        bpmnModel.getMainProcess().setId(flowEntry.getProcessDefinitionKey());
        flowApiService.addProcessInstanceEndListener(bpmnModel, FlowFinishedListener.class);
        Collection<FlowElement> elementList = bpmnModel.getMainProcess().getFlowElements();
        List<FlowElement> sequenceFlowList =
                elementList.stream().filter(SequenceFlow.class::isInstance).collect(Collectors.toList());
        for (FlowElement sequenceFlow : sequenceFlowList) {
            FlowElementExtProperty extProperty = flowTaskExtService.buildFlowElementExt(sequenceFlow);
            if (extProperty != null && extProperty.getLatestApprovalStatus() != null) {
                List<FieldExtension> fieldExtensions = new LinkedList<>();
                FieldExtension fieldExtension = new FieldExtension();
                fieldExtension.setFieldName(FlowConstant.LATEST_APPROVAL_STATUS_KEY);
                fieldExtension.setStringValue(extProperty.getLatestApprovalStatus().toString());
                fieldExtensions.add(fieldExtension);
                flowApiService.addExecutionListener(
                        sequenceFlow, UpdateLatestApprovalStatusListener.class, "start", fieldExtensions);
            }
        }
        List<FlowTaskExt> flowTaskExtList = flowTaskExtService.buildTaskExtList(bpmnModel);
        if (StrUtil.isNotBlank(flowEntry.getExtensionData())) {
            this.mergeTaskNotifyData(flowEntry, flowTaskExtList);
        }
        this.processFlowTaskExtList(flowTaskExtList, elementList);
        TokenData tokenData = TokenData.takeFromRequest();
        Deployment deploy = repositoryService.createDeployment()
                .addBpmnModel(flowEntry.getProcessDefinitionKey() + ".bpmn", bpmnModel)
                .tenantId(tokenData.getTenantId() != null ? tokenData.getTenantId().toString() : tokenData.getAppCode())
                .name(flowEntry.getProcessDefinitionName())
                .key(flowEntry.getProcessDefinitionKey())
                .category(flowCategory.getCode())
                .deploy();
        ProcessDefinition processDefinition = flowApiService.getProcessDefinitionByDeployId(deploy.getId());
        FlowEntryPublish flowEntryPublish = new FlowEntryPublish();
        flowEntryPublish.setEntryPublishId(idGenerator.nextLongId());
        flowEntryPublish.setEntryId(flowEntry.getEntryId());
        flowEntryPublish.setProcessDefinitionId(processDefinition.getId());
        flowEntryPublish.setDeployId(processDefinition.getDeploymentId());
        flowEntryPublish.setPublishVersion(processDefinition.getVersion());
        flowEntryPublish.setActiveStatus(true);
        flowEntryPublish.setMainVersion(flowEntry.getStatus().equals(FlowEntryStatus.UNPUBLISHED));
        flowEntryPublish.setCreateUserId(TokenData.takeFromRequest().getUserId());
        flowEntryPublish.setPublishTime(new Date());
        flowEntryPublish.setInitTaskInfo(initTaskInfo);
        AnalyzedNode rootNode = flowApiService.analyzeBpmnRoads(processDefinition.getId());
        flowEntryPublish.setAnalyzedNodeJson(JSON.toJSONString(rootNode));
        flowEntryPublishMapper.insert(flowEntryPublish);
        FlowEntry updatedFlowEntry = new FlowEntry();
        updatedFlowEntry.setEntryId(flowEntry.getEntryId());
        updatedFlowEntry.setStatus(FlowEntryStatus.PUBLISHED);
        updatedFlowEntry.setLatestPublishTime(new Date());
        // 对于从未发布过的工作，第一次发布的时候会将本地发布置位主版本。
        if (flowEntry.getStatus().equals(FlowEntryStatus.UNPUBLISHED)) {
            updatedFlowEntry.setMainEntryPublishId(flowEntryPublish.getEntryPublishId());
        }
        flowEntryMapper.updateById(updatedFlowEntry);
        FlowEntryVariable flowEntryVariableFilter = new FlowEntryVariable();
        flowEntryVariableFilter.setEntryId(flowEntry.getEntryId());
        List<FlowEntryVariable> flowEntryVariableList =
                flowEntryVariableService.getFlowEntryVariableList(flowEntryVariableFilter, null);
        if (CollUtil.isNotEmpty(flowTaskExtList)) {
            flowTaskExtList.forEach(t -> t.setProcessDefinitionId(processDefinition.getId()));
            flowTaskExtService.saveBatch(flowTaskExtList);
        }
        this.insertEntryPublishVariables(flowEntryVariableList, flowEntryPublish.getEntryPublishId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public  boolean updateVaribleDisplay(List<FlowVaribleDisplayDto> flowVaribleDisplayList) {
        System.out.println("\n%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        for(int i = 0; i < flowVaribleDisplayList.size();i++){
            FlowVaribleDisplayDto flowVaribleDisplayDto = flowVaribleDisplayList.get(i);
            System.out.println(flowVaribleDisplayDto.toString());
//            flowVariableDisplayMapper.deleteVariable(flowVaribleDisplayDto);
//            flowVariableDisplayMapper.add(flowVaribleDisplayDto);
        }
        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean update(FlowEntry flowEntry, FlowEntry originalFlowEntry) {
        commonRedisUtil.evictFormCache(
                FlowRedisKeyUtil.makeFlowEntryKey(flowEntry.getProcessDefinitionKey()));
        TokenData tokenData = TokenData.takeFromRequest();
        flowEntry.setAppCode(tokenData.getAppCode());
        flowEntry.setTenantId(tokenData.getTenantId());
        flowEntry.setUpdateUserId(tokenData.getUserId());
        flowEntry.setCreateUserId(originalFlowEntry.getCreateUserId());
        flowEntry.setUpdateTime(new Date());
        flowEntry.setCreateTime(originalFlowEntry.getCreateTime());
        flowEntry.setPageId(originalFlowEntry.getPageId());
        return flowEntryMapper.updateById(flowEntry) == 1;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean remove(Long entryId) {
        FlowEntry flowEntry = this.getById(entryId);
        if (flowEntry != null) {
            commonRedisUtil.evictFormCache(
                    FlowRedisKeyUtil.makeFlowEntryKey(flowEntry.getProcessDefinitionKey()));
        }
        if (flowEntryMapper.deleteById(entryId) != 1) {
            return false;
        }
        flowEntryVariableService.removeByEntryId(entryId);
        return true;
    }

    @Override
    public List<FlowEntry> getFlowEntryList(FlowEntry filter, String orderBy) {
        if (filter == null) {
            filter = new FlowEntry();
        }
        TokenData tokenData = TokenData.takeFromRequest();
        filter.setTenantId(tokenData.getTenantId());
        filter.setAppCode(tokenData.getAppCode());
        return flowEntryMapper.getFlowEntryList(filter, orderBy);
    }

    @Override
    public List<FlowEntry> getFlowEntryListWithRelation(FlowEntry filter, String orderBy) {
        List<FlowEntry> resultList = this.getFlowEntryList(filter, orderBy);
        // 在缺省生成的代码中，如果查询结果resultList不是Page对象，说明没有分页，那么就很可能是数据导出接口调用了当前方法。
        // 为了避免一次性的大量数据关联，规避因此而造成的系统运行性能冲击，这里手动进行了分批次读取，开发者可按需修改该值。
        int batchSize = resultList instanceof Page ? 0 : 1000;
        this.buildRelationForDataList(resultList, MyRelationParam.normal(), batchSize);
        Set<Long> mainEntryPublishIdSet = resultList.stream().filter(e -> e.getMainEntryPublishId() != null)
                .map(FlowEntry::getMainEntryPublishId).collect(Collectors.toSet());
        if (CollUtil.isNotEmpty(mainEntryPublishIdSet)) {
            List<FlowEntryPublish> mainEntryPublishList =
                    flowEntryPublishMapper.selectBatchIds(mainEntryPublishIdSet);
            MyModelUtil.makeOneToOneRelation(FlowEntry.class, resultList, FlowEntry::getMainEntryPublishId,
                    mainEntryPublishList, FlowEntryPublish::getEntryPublishId, "mainFlowEntryPublish");
        }
        return resultList;
    }

    @Override
    public FlowEntry getFlowEntryFromCache(String processDefinitionKey) {
        String key = FlowRedisKeyUtil.makeFlowEntryKey(processDefinitionKey);
        LambdaQueryWrapper<FlowEntry> qw = new LambdaQueryWrapper<>();
        qw.eq(FlowEntry::getProcessDefinitionKey, processDefinitionKey);
        TokenData tokenData = TokenData.takeFromRequest();
        if (StrUtil.isNotBlank(tokenData.getAppCode())) {
            qw.eq(FlowEntry::getAppCode, tokenData.getAppCode());
        } else {
            qw.isNull(FlowEntry::getAppCode);
        }
        if (tokenData.getTenantId() != null) {
            qw.eq(FlowEntry::getTenantId, tokenData.getTenantId());
        } else {
            qw.isNull(FlowEntry::getTenantId);
        }
        return commonRedisUtil.getFromCache(key, qw, flowEntryMapper::selectOne, FlowEntry.class);
    }

    @Override
    public List<FlowEntryPublish> getFlowEntryPublishList(Long entryId) {
        FlowEntryPublish filter = new FlowEntryPublish();
        filter.setEntryId(entryId);
        LambdaQueryWrapper<FlowEntryPublish> queryWrapper = new LambdaQueryWrapper<>(filter);
        queryWrapper.orderByDesc(FlowEntryPublish::getEntryPublishId);
        return flowEntryPublishMapper.selectList(queryWrapper);
    }

    @Override
    public List<FlowEntryPublish> getFlowEntryPublishList(Set<String> processDefinitionIdSet) {
        LambdaQueryWrapper<FlowEntryPublish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(FlowEntryPublish::getProcessDefinitionId, processDefinitionIdSet);
        return flowEntryPublishMapper.selectList(queryWrapper);
    }

    @Override
    public FlowEntryPublish getFlowEntryPublishFromCache(Long entryPublishId) {
        String key = FlowRedisKeyUtil.makeFlowEntryPublishKey(entryPublishId);
        return commonRedisUtil.getFromCache(
                key, entryPublishId, flowEntryPublishMapper::selectById, FlowEntryPublish.class, FLOW_ENTRY_PUBLISH_TTL);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateFlowEntryMainVersion(FlowEntry flowEntry, FlowEntryPublish newMainFlowEntryPublish) {
        commonRedisUtil.evictFormCache(
                FlowRedisKeyUtil.makeFlowEntryKey(flowEntry.getProcessDefinitionKey()));
        commonRedisUtil.evictFormCache(
                FlowRedisKeyUtil.makeFlowEntryPublishKey(newMainFlowEntryPublish.getEntryPublishId()));
        FlowEntryPublish oldMainFlowEntryPublish =
                flowEntryPublishMapper.selectById(flowEntry.getMainEntryPublishId());
        if (oldMainFlowEntryPublish != null) {
            commonRedisUtil.evictFormCache(
                    FlowRedisKeyUtil.makeFlowEntryPublishKey(oldMainFlowEntryPublish.getEntryPublishId()));
            oldMainFlowEntryPublish.setMainVersion(false);
            flowEntryPublishMapper.updateById(oldMainFlowEntryPublish);
        }
        newMainFlowEntryPublish.setMainVersion(true);
        flowEntryPublishMapper.updateById(newMainFlowEntryPublish);
        FlowEntry updatedEntry = new FlowEntry();
        updatedEntry.setEntryId(flowEntry.getEntryId());
        updatedEntry.setMainEntryPublishId(newMainFlowEntryPublish.getEntryPublishId());
        flowEntryMapper.updateById(updatedEntry);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void suspendFlowEntryPublish(FlowEntryPublish flowEntryPublish) {
        commonRedisUtil.evictFormCache(
                FlowRedisKeyUtil.makeFlowEntryPublishKey(flowEntryPublish.getEntryPublishId()));
        FlowEntryPublish updatedEntryPublish = new FlowEntryPublish();
        updatedEntryPublish.setEntryPublishId(flowEntryPublish.getEntryPublishId());
        updatedEntryPublish.setActiveStatus(false);
        flowEntryPublishMapper.updateById(updatedEntryPublish);
        flowApiService.suspendProcessDefinition(flowEntryPublish.getProcessDefinitionId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void activateFlowEntryPublish(FlowEntryPublish flowEntryPublish) {
        commonRedisUtil.evictFormCache(
                FlowRedisKeyUtil.makeFlowEntryPublishKey(flowEntryPublish.getEntryPublishId()));
        FlowEntryPublish updatedEntryPublish = new FlowEntryPublish();
        updatedEntryPublish.setEntryPublishId(flowEntryPublish.getEntryPublishId());
        updatedEntryPublish.setActiveStatus(true);
        flowEntryPublishMapper.updateById(updatedEntryPublish);
        flowApiService.activateProcessDefinition(flowEntryPublish.getProcessDefinitionId());
    }

    @Override
    public boolean existByProcessDefinitionKey(String processDefinitionKey) {
        FlowEntry filter = new FlowEntry();
        filter.setProcessDefinitionKey(processDefinitionKey);
        return CollUtil.isNotEmpty(this.getFlowEntryList(filter, null));
    }

    @Override
    public CallResult verifyRelatedData(FlowEntry flowEntry, FlowEntry originalFlowEntry) {
        String errorMessageFormat = "数据验证失败，关联的%s并不存在，请刷新后重试！";
        if (this.needToVerify(flowEntry, originalFlowEntry, FlowEntry::getCategoryId)
                && !flowCategoryService.existId(flowEntry.getCategoryId())) {
            return CallResult.error(String.format(errorMessageFormat, "流程类别Id"));
        }
        return CallResult.ok();
    }

    private void insertBuiltinEntryVariables(Long entryId) {
        Date now = new Date();
        FlowEntryVariable operationTypeVariable = new FlowEntryVariable();
        operationTypeVariable.setVariableId(idGenerator.nextLongId());
        operationTypeVariable.setEntryId(entryId);
        operationTypeVariable.setVariableName(FlowConstant.OPERATION_TYPE_VAR);
        operationTypeVariable.setShowName("审批类型");
        operationTypeVariable.setVariableType(FlowVariableType.TASK);
        operationTypeVariable.setBuiltin(true);
        operationTypeVariable.setCreateTime(now);
        flowEntryVariableService.saveNew(operationTypeVariable);
        FlowEntryVariable startUserNameVariable = new FlowEntryVariable();
        startUserNameVariable.setVariableId(idGenerator.nextLongId());
        startUserNameVariable.setEntryId(entryId);
        startUserNameVariable.setVariableName("startUserName");
        startUserNameVariable.setShowName("流程启动用户");
        startUserNameVariable.setVariableType(FlowVariableType.INSTANCE);
        startUserNameVariable.setBuiltin(true);
        startUserNameVariable.setCreateTime(now);
        flowEntryVariableService.saveNew(startUserNameVariable);
    }

    private void insertEntryPublishVariables(List<FlowEntryVariable> entryVariableList, Long entryPublishId) {
        if (CollUtil.isEmpty(entryVariableList)) {
            return;
        }
        List<FlowEntryPublishVariable> entryPublishVariableList =
                MyModelUtil.copyCollectionTo(entryVariableList, FlowEntryPublishVariable.class);
        for (FlowEntryPublishVariable variable : entryPublishVariableList) {
            variable.setVariableId(idGenerator.nextLongId());
            variable.setEntryPublishId(entryPublishId);
        }
        flowEntryPublishVariableMapper.insertList(entryPublishVariableList);
    }

    private void mergeTaskNotifyData(FlowEntry flowEntry, List<FlowTaskExt> flowTaskExtList) {
        FlowEntryExtensionData flowEntryExtensionData =
                JSON.parseObject(flowEntry.getExtensionData(), FlowEntryExtensionData.class);
        if (CollUtil.isEmpty(flowEntryExtensionData.getNotifyTypes())) {
            return;
        }
        List<String> flowTaskNotifyTypes =
                flowEntryExtensionData.getNotifyTypes().stream().filter(StrUtil::isNotBlank).collect(Collectors.toList());
        if (CollUtil.isEmpty(flowTaskNotifyTypes)) {
            return;
        }
        for (FlowTaskExt flowTaskExt : flowTaskExtList) {
            if (flowTaskExt.getExtraDataJson() == null) {
                JSONObject o = new JSONObject();
                o.put(FlowConstant.USER_TASK_NOTIFY_TYPES_KEY, flowTaskNotifyTypes);
                flowTaskExt.setExtraDataJson(o.toJSONString());
            } else {
                FlowUserTaskExtData taskExtData =
                        JSON.parseObject(flowTaskExt.getExtraDataJson(), FlowUserTaskExtData.class);
                if (CollUtil.isEmpty(taskExtData.getFlowNotifyTypeList())) {
                    taskExtData.setFlowNotifyTypeList(flowTaskNotifyTypes);
                } else {
                    Set<String> notifyTypesSet = taskExtData.getFlowNotifyTypeList()
                            .stream().filter(StrUtil::isNotBlank).collect(Collectors.toSet());
                    notifyTypesSet.addAll(flowTaskNotifyTypes);
                    taskExtData.setFlowNotifyTypeList(new LinkedList<>(notifyTypesSet));
                }
                flowTaskExt.setExtraDataJson(JSON.toJSONString(taskExtData));
            }
        }
    }

    private void processFlowTaskExtList(List<FlowTaskExt> flowTaskExtList, Collection<FlowElement> elementList) {
        Map<String, FlowElement> elementMap = elementList.stream()
                .filter(UserTask.class::isInstance).collect(Collectors.toMap(FlowElement::getId, c -> c));
        BaseFlowIdentityExtHelper flowIdentityExtHelper = flowCustomExtFactory.getFlowIdentityExtHelper();
        for (FlowTaskExt t : flowTaskExtList) {
            UserTask userTask = (UserTask) elementMap.get(t.getTaskId());
            flowApiService.addTaskCreateListener(userTask, FlowUserTaskListener.class);
            Map<String, List<ExtensionAttribute>> attributes = userTask.getAttributes();
            if (CollUtil.isNotEmpty(attributes.get(FlowConstant.USER_TASK_AUTO_SKIP_KEY))) {
                flowApiService.addExecutionListener(
                        userTask, AutoSkipListener.class, "start", null);
            }
            // 如果流程图中包含部门领导审批和上级部门领导审批的选项，就需要注册 FlowCustomExtFactory 工厂中的
            // BaseFlowIdentityExtHelper 对象，该注册操作需要业务模块中实现。
            if (StrUtil.equals(t.getGroupType(), FlowConstant.GROUP_TYPE_UP_DEPT_POST_LEADER)) {
                userTask.setCandidateGroups(
                        CollUtil.newArrayList("${" + FlowConstant.GROUP_TYPE_UP_DEPT_POST_LEADER_VAR + "}"));
                Assert.notNull(flowIdentityExtHelper);
                flowApiService.addTaskCreateListener(userTask, flowIdentityExtHelper.getUpDeptPostLeaderListener());
            } else if (StrUtil.equals(t.getGroupType(), FlowConstant.GROUP_TYPE_DEPT_POST_LEADER)) {
                userTask.setCandidateGroups(
                        CollUtil.newArrayList("${" + FlowConstant.GROUP_TYPE_DEPT_POST_LEADER_VAR + "}"));
                Assert.notNull(flowIdentityExtHelper);
                flowApiService.addTaskCreateListener(userTask, flowIdentityExtHelper.getDeptPostLeaderListener());
            } else if (StrUtil.equals(t.getGroupType(), FlowConstant.GROUP_TYPE_POST)) {
                Assert.notNull(t.getDeptPostListJson());
                List<FlowTaskPostCandidateGroup> groupDataList =
                        JSON.parseArray(t.getDeptPostListJson(), FlowTaskPostCandidateGroup.class);
                List<String> candidateGroupList =
                        FlowTaskPostCandidateGroup.buildCandidateGroupList(groupDataList);
                userTask.setCandidateGroups(candidateGroupList);
            }
            if (t.getExtraDataJson() != null) {
                FlowUserTaskExtData userTaskExtData = JSON.parseObject(t.getExtraDataJson(), FlowUserTaskExtData.class);
                if (CollUtil.isNotEmpty(userTaskExtData.getFlowNotifyTypeList())) {
                    flowApiService.addTaskCreateListener(userTask, FlowTaskNotifyListener.class);
                }
                if (userTaskExtData.getRejectType().equals(FlowUserTaskExtData.REJECT_TYPE_BACK_TO_SOURCE)) {
                    flowApiService.addTaskCreateListener(userTask, FlowTaskRejectBackListener.class);
                }
            }
        }
    }
}
