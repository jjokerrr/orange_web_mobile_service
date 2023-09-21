package com.bupt.common.online.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bupt.common.core.cache.CacheConfig;
import com.bupt.common.core.annotation.MyRequestBody;
import com.bupt.common.core.constant.ErrorCodeEnum;
import com.bupt.common.core.object.*;
import com.bupt.common.core.util.MyCommonUtil;
import com.bupt.common.core.util.MyModelUtil;
import com.bupt.common.core.util.MyPageUtil;
import com.bupt.common.core.validator.UpdateGroup;
import com.bupt.common.log.annotation.OperationLog;
import com.bupt.common.log.model.constant.SysOperationLogType;
import com.bupt.common.online.config.OnlineProperties;
import com.bupt.common.online.dto.OnlineFormDto;
import com.bupt.common.online.model.*;
import com.bupt.common.online.service.*;
import com.bupt.common.online.vo.OnlineFormVo;
import com.github.pagehelper.page.PageMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.groups.Default;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 在线表单表单接口。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Slf4j
@RestController
@RequestMapping("${common-online.urlPrefix}/onlineForm")
@ConditionalOnProperty(name = "common-online.operationEnabled", havingValue = "true")
public class OnlineFormController {

    @Autowired
    private OnlineFormService onlineFormService;
    @Autowired
    private OnlineDatasourceService onlineDatasourceService;
    @Autowired
    private OnlineDatasourceRelationService onlineDatasourceRelationService;
    @Autowired
    private OnlineTableService onlineTableService;
    @Autowired
    private OnlineColumnService onlineColumnService;
    @Autowired
    private OnlineVirtualColumnService onlineVirtualColumnService;
    @Autowired
    private OnlineDictService onlineDictService;
    @Autowired
    private OnlineRuleService onlineRuleService;
    @Autowired
    private OnlineProperties properties;
    @Resource(name = "caffeineCacheManager")
    private CacheManager cacheManager;

    /**
     * 新增在线表单数据。
     *
     * @param onlineFormDto 新增对象。
     * @return 应答结果对象，包含新增对象主键Id。
     */
    @OperationLog(type = SysOperationLogType.ADD)
    @PostMapping("/add")
    public ResponseResult<Long> add(@MyRequestBody OnlineFormDto onlineFormDto) {
        String errorMessage = MyCommonUtil.getModelValidationError(onlineFormDto);
        if (errorMessage != null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        OnlineForm onlineForm = MyModelUtil.copyTo(onlineFormDto, OnlineForm.class);
        if (onlineFormService.existByFormCode(onlineForm.getFormCode())) {
            errorMessage = "数据验证失败，表单编码已经存在！";
            return ResponseResult.error(ErrorCodeEnum.DUPLICATED_UNIQUE_KEY, errorMessage);
        }
        // 验证关联Id的数据合法性
        CallResult callResult = onlineFormService.verifyRelatedData(onlineForm, null);
        if (!callResult.isSuccess()) {
            errorMessage = callResult.getErrorMessage();
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        Set<Long> datasourceIdSet = null;
        if (CollUtil.isNotEmpty(onlineFormDto.getDatasourceIdList())) {
            ResponseResult<Set<Long>> verifyDatasourceIdsResult =
                    this.doVerifyDatasourceIdsAndGet(onlineFormDto.getDatasourceIdList());
            if (!verifyDatasourceIdsResult.isSuccess()) {
                return ResponseResult.errorFrom(verifyDatasourceIdsResult);
            }
            datasourceIdSet = verifyDatasourceIdsResult.getData();
        }
        onlineForm = onlineFormService.saveNew(onlineForm, datasourceIdSet);
        return ResponseResult.success(onlineForm.getFormId());
    }

    /**
     * 更新在线表单数据。
     *
     * @param onlineFormDto 更新对象。
     * @return 应答结果对象。
     */
    @OperationLog(type = SysOperationLogType.UPDATE)
    @PostMapping("/update")
    public ResponseResult<Void> update(@MyRequestBody OnlineFormDto onlineFormDto) {
        String errorMessage = MyCommonUtil.getModelValidationError(onlineFormDto, Default.class, UpdateGroup.class);
        if (errorMessage != null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        OnlineForm onlineForm = MyModelUtil.copyTo(onlineFormDto, OnlineForm.class);
        ResponseResult<OnlineForm> verifyResult = this.doVerifyAndGet(onlineForm.getFormId());
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        OnlineForm originalOnlineForm = verifyResult.getData();
        // 验证关联Id的数据合法性
        CallResult callResult = onlineFormService.verifyRelatedData(onlineForm, originalOnlineForm);
        if (!callResult.isSuccess()) {
            errorMessage = callResult.getErrorMessage();
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        if (!StrUtil.equals(onlineForm.getFormCode(), originalOnlineForm.getFormCode())
                && onlineFormService.existByFormCode(onlineForm.getFormCode())) {
            errorMessage = "数据验证失败，表单编码已经存在！";
            return ResponseResult.error(ErrorCodeEnum.DUPLICATED_UNIQUE_KEY, errorMessage);
        }
        Set<Long> datasourceIdSet = null;
        if (CollUtil.isNotEmpty(onlineFormDto.getDatasourceIdList())) {
            ResponseResult<Set<Long>> verifyDatasourceIdsResult =
                    this.doVerifyDatasourceIdsAndGet(onlineFormDto.getDatasourceIdList());
            if (!verifyDatasourceIdsResult.isSuccess()) {
                return ResponseResult.errorFrom(verifyDatasourceIdsResult);
            }
            datasourceIdSet = verifyDatasourceIdsResult.getData();
        }
        if (!onlineFormService.update(onlineForm, originalOnlineForm, datasourceIdSet)) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        return ResponseResult.success();
    }

    /**
     * 删除在线表单数据。
     *
     * @param formId 删除对象主键Id。
     * @return 应答结果对象。
     */
    @OperationLog(type = SysOperationLogType.DELETE)
    @PostMapping("/delete")
    public ResponseResult<Void> delete(@MyRequestBody Long formId) {
        String errorMessage;
        ResponseResult<OnlineForm> verifyResult = this.doVerifyAndGet(formId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        if (!onlineFormService.remove(formId)) {
            errorMessage = "数据操作失败，删除的对象不存在，请刷新后重试！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        return ResponseResult.success();
    }

    /**
     * 克隆一个在线表单对象。
     *
     * @param formId 源表单主键Id。
     * @return 新克隆表单主键Id。
     */
    @OperationLog(type = SysOperationLogType.ADD)
    @PostMapping("/clone")
    public ResponseResult<Long> clone(@MyRequestBody Long formId) {
        ResponseResult<OnlineForm> verifyResult = this.doVerifyAndGet(formId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        OnlineForm form = verifyResult.getData();
        form.setFormName(form.getFormName() + "_copy");
        form.setFormCode(form.getFormCode() + "_copy_" + System.currentTimeMillis());
        List<OnlineFormDatasource> formDatasourceList = onlineFormService.getFormDatasourceListFromCache(formId);
        Set<Long> datasourceIdSet = formDatasourceList.stream()
                .map(OnlineFormDatasource::getDatasourceId).collect(Collectors.toSet());
        onlineFormService.saveNew(form, datasourceIdSet);
        return ResponseResult.success(form.getFormId());
    }

    /**
     * 列出符合过滤条件的在线表单列表。
     *
     * @param onlineFormDtoFilter 过滤对象。
     * @param orderParam          排序参数。
     * @param pageParam           分页参数。
     * @return 应答结果对象，包含查询结果集。
     */
    @PostMapping("/list")
    public ResponseResult<MyPageData<OnlineFormVo>> list(
            @MyRequestBody OnlineFormDto onlineFormDtoFilter,
            @MyRequestBody MyOrderParam orderParam,
            @MyRequestBody MyPageParam pageParam) {
        if (pageParam != null) {
            PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize());
        }
        OnlineForm onlineFormFilter = MyModelUtil.copyTo(onlineFormDtoFilter, OnlineForm.class);
        String orderBy = MyOrderParam.buildOrderBy(orderParam, OnlineForm.class);
        List<OnlineForm> onlineFormList =
                onlineFormService.getOnlineFormListWithRelation(onlineFormFilter, orderBy);
        return ResponseResult.success(MyPageUtil.makeResponseData(onlineFormList, OnlineForm.INSTANCE));
    }

    /**
     * 查看指定在线表单对象详情。
     *
     * @param formId 指定对象主键Id。
     * @return 应答结果对象，包含对象详情。
     */
    @GetMapping("/view")
    public ResponseResult<OnlineFormVo> view(@RequestParam Long formId) {
        ResponseResult<OnlineForm> verifyResult = this.doVerifyAndGet(formId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        OnlineForm onlineForm = onlineFormService.getByIdWithRelation(formId, MyRelationParam.full());
        OnlineFormVo onlineFormVo = OnlineForm.INSTANCE.fromModel(onlineForm);
        List<OnlineFormDatasource> formDatasourceList = onlineFormService.getFormDatasourceListFromCache(formId);
        if (CollUtil.isNotEmpty(formDatasourceList)) {
            onlineFormVo.setDatasourceIdList(formDatasourceList.stream()
                    .map(OnlineFormDatasource::getDatasourceId).collect(Collectors.toList()));
        }
        return ResponseResult.success(onlineFormVo);
    }

    /**
     * 获取指定在线表单对象在前端渲染时所需的所有数据对象。
     *
     * @param formId 指定对象主键Id。
     * @return 应答结果对象，包含对象详情。
     */
    @GetMapping("/render")
    public ResponseResult<JSONObject> render(@RequestParam Long formId) {
        String errorMessage;
        Cache cache = null;
        if (BooleanUtil.isTrue(properties.getEnableRenderCache())) {
            cache = cacheManager.getCache(CacheConfig.CacheEnum.ONLINE_FORM_RENDER_CACCHE.name());
            Assert.notNull(cache, "Cache ONLINE_FORM_RENDER_CACCHE can't be NULL");
            JSONObject responseData = cache.get(formId, JSONObject.class);
            if (responseData != null) {
                Object appCode = responseData.get("appCode");
                if (ObjectUtil.notEqual(appCode, TokenData.takeFromRequest().getAppCode())) {
                    errorMessage = "数据验证失败，当前应用不包含该表单Id!";
                    return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
                }
                return ResponseResult.success(responseData);
            }
        }
        OnlineForm onlineForm = onlineFormService.getOnlineFormFromCache(formId);
        if (onlineForm == null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        OnlineFormVo onlineFormVo = OnlineForm.INSTANCE.fromModel(onlineForm);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("onlineForm", onlineFormVo);
        List<OnlineFormDatasource> formDatasourceList = onlineFormService.getFormDatasourceListFromCache(formId);
        if (CollUtil.isEmpty(formDatasourceList)) {
            return ResponseResult.success(jsonObject);
        }
        Set<Long> datasourceIdSet = formDatasourceList.stream()
                .map(OnlineFormDatasource::getDatasourceId).collect(Collectors.toSet());
        List<OnlineDatasource> onlineDatasourceList =
                onlineDatasourceService.getOnlineDatasourceListFromCache(datasourceIdSet);
        jsonObject.put("onlineDatasourceList", onlineDatasourceList);
        Set<Long> tableIdSet = onlineDatasourceList.stream()
                .map(OnlineDatasource::getMasterTableId).collect(Collectors.toSet());
        List<OnlineDatasourceRelation> onlineDatasourceRelationList =
                onlineDatasourceRelationService.getOnlineDatasourceRelationListFromCache(datasourceIdSet);
        if (CollUtil.isNotEmpty(onlineDatasourceRelationList)) {
            jsonObject.put("onlineDatasourceRelationList", onlineDatasourceRelationList);
            tableIdSet.addAll(onlineDatasourceRelationList.stream()
                    .map(OnlineDatasourceRelation::getSlaveTableId).collect(Collectors.toList()));
        }
        List<OnlineTable> onlineTableList = new LinkedList<>();
        List<OnlineColumn> onlineColumnList = new LinkedList<>();
        for (Long tableId : tableIdSet) {
            OnlineTable table = onlineTableService.getOnlineTableFromCache(tableId);
            onlineTableList.add(table);
            onlineColumnList.addAll(table.getColumnMap().values());
            table.setColumnMap(null);
        }
        jsonObject.put("onlineTableList", onlineTableList);
        jsonObject.put("onlineColumnList", onlineColumnList);
        List<OnlineVirtualColumn> virtualColumnList =
                onlineVirtualColumnService.getOnlineVirtualColumnListByTableIds(tableIdSet);
        jsonObject.put("onlineVirtualColumnList", virtualColumnList);
        Set<Long> dictIdSet = onlineColumnList.stream()
                .filter(c -> c.getDictId() != null).map(OnlineColumn::getDictId).collect(Collectors.toSet());
        Set<Long> widgetDictIdSet = this.extractDictIdSetFromWidgetJson(onlineForm.getWidgetJson());
        CollUtil.addAll(dictIdSet, widgetDictIdSet);
        if (CollUtil.isNotEmpty(dictIdSet)) {
            List<OnlineDict> onlineDictList = onlineDictService.getOnlineDictListFromCache(dictIdSet);
            if (onlineDictList.size() != dictIdSet.size()) {
                Set<Long> columnDictIdSet = onlineDictList.stream().map(OnlineDict::getDictId).collect(Collectors.toSet());
                Long notExistDictId = this.findNotExistDictId(dictIdSet, columnDictIdSet);
                Assert.notNull(notExistDictId, "notExistDictId can't be NULL");
                errorMessage = String.format("数据验证失败，字典Id [%s] 不存在！", notExistDictId);
                return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
            }
            jsonObject.put("onlineDictList", onlineDictList);
        }
        Set<Long> columnIdSet = onlineColumnList.stream().map(OnlineColumn::getColumnId).collect(Collectors.toSet());
        List<OnlineColumnRule> colunmRuleList = onlineRuleService.getOnlineColumnRuleListByColumnIds(columnIdSet);
        if (CollUtil.isNotEmpty(colunmRuleList)) {
            jsonObject.put("onlineColumnRuleList", colunmRuleList);
        }
        jsonObject.put("appCode", TokenData.takeFromRequest().getAppCode());
        if (BooleanUtil.isTrue(properties.getEnableRenderCache())) {
            Assert.notNull(cache, "Cache ONLINE_FORM_RENDER_CACCHE can't be NULL");
            cache.put(formId, jsonObject);
        }
        return ResponseResult.success(jsonObject);
    }

    private Long findNotExistDictId(Set<Long> originalDictIdSet, Set<Long> dictIdSet) {
        return originalDictIdSet.stream().filter(d -> !dictIdSet.contains(d)).findFirst().orElse(null);
    }

    private ResponseResult<OnlineForm> doVerifyAndGet(Long formId) {
        String errorMessage;
        if (MyCommonUtil.existBlankArgument(formId)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        // 验证关联Id的数据合法性
        OnlineForm form = onlineFormService.getById(formId);
        if (form == null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        if (!StrUtil.equals(form.getAppCode(), TokenData.takeFromRequest().getAppCode())) {
            errorMessage = "数据验证失败，当前应用不包含该表单！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        if (ObjectUtil.notEqual(form.getTenantId(), TokenData.takeFromRequest().getTenantId())) {
            errorMessage = "数据验证失败，当前租户不包含该表单！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        return ResponseResult.success(form);
    }

    private ResponseResult<Set<Long>> doVerifyDatasourceIdsAndGet(List<Long> datasourceIdList) {
        String errorMessage;
        Set<Long> datasourceIdSet = new HashSet<>(datasourceIdList);
        List<OnlineDatasource> datasourceList = onlineDatasourceService.getInList(datasourceIdSet);
        if (datasourceIdSet.size() != datasourceList.size()) {
            errorMessage = "数据验证失败，当前在线表单包含不存在的数据源Id！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        String appCode = TokenData.takeFromRequest().getAppCode();
        for (OnlineDatasource datasource : datasourceList) {
            if (!StrUtil.equals(datasource.getAppCode(), appCode)) {
                errorMessage = "数据验证失败，存在不是当前应用的数据源！";
                return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
            }
        }
        return ResponseResult.success(datasourceIdSet);
    }

    private Set<Long> extractDictIdSetFromWidgetJson(String widgetJson) {
        Set<Long> dictIdSet = new HashSet<>();
        if (StrUtil.isBlank(widgetJson)) {
            return dictIdSet;
        }
        JSONObject allData = JSON.parseObject(widgetJson);
        JSONObject pcData = allData.getJSONObject("pc");
        if (MapUtil.isEmpty(pcData)) {
            return dictIdSet;
        }
        JSONArray widgetListArray = pcData.getJSONArray("widgetList");
        if (CollUtil.isEmpty(widgetListArray)) {
            return dictIdSet;
        }
        for (int i = 0; i < widgetListArray.size(); i++) {
            this.recursiveExtractDictId(widgetListArray.getJSONObject(i), dictIdSet);
        }
        return dictIdSet;
    }

    private void recursiveExtractDictId(JSONObject widgetData, Set<Long> dictIdSet) {
        JSONObject propsData = widgetData.getJSONObject("props");
        if (MapUtil.isNotEmpty(propsData)) {
            JSONObject dictInfoData = propsData.getJSONObject("dictInfo");
            if (MapUtil.isNotEmpty(dictInfoData)) {
                Long dictId = dictInfoData.getLong("dictId");
                if (dictId != null) {
                    dictIdSet.add(dictId);
                }
            }
        }
        JSONArray childWidgetArray = widgetData.getJSONArray("childWidgetList");
        if (CollUtil.isNotEmpty(childWidgetArray)) {
            for (int i = 0; i < childWidgetArray.size(); i++) {
                this.recursiveExtractDictId(childWidgetArray.getJSONObject(i), dictIdSet);
            }
        }
    }
}
