package com.bupt.common.online.controller;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.bupt.common.core.annotation.MyRequestBody;
import com.bupt.common.core.constant.ErrorCodeEnum;
import com.bupt.common.core.object.*;
import com.bupt.common.core.util.MyCommonUtil;
import com.bupt.common.core.util.MyModelUtil;
import com.bupt.common.core.util.MyPageUtil;
import com.bupt.common.core.validator.UpdateGroup;
import com.bupt.common.log.annotation.OperationLog;
import com.bupt.common.log.model.constant.SysOperationLogType;
import com.bupt.common.online.dto.OnlineDatasourceDto;
import com.bupt.common.online.dto.OnlinePageDatasourceDto;
import com.bupt.common.online.dto.OnlinePageDto;
import com.bupt.common.online.model.OnlineDatasource;
import com.bupt.common.online.model.OnlineForm;
import com.bupt.common.online.model.OnlinePage;
import com.bupt.common.online.model.OnlinePageDatasource;
import com.bupt.common.online.model.constant.PageStatus;
import com.bupt.common.online.service.OnlineDatasourceService;
import com.bupt.common.online.service.OnlineFormService;
import com.bupt.common.online.service.OnlinePageService;
import com.bupt.common.online.vo.OnlineDatasourceVo;
import com.bupt.common.online.vo.OnlinePageDatasourceVo;
import com.bupt.common.online.vo.OnlinePageVo;
import com.github.pagehelper.page.PageMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;

import javax.validation.groups.Default;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 在线表单页面接口。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Slf4j
@RestController
@RequestMapping("${common-online.urlPrefix}/onlinePage")
@ConditionalOnProperty(name = "common-online.operationEnabled", havingValue = "true")
public class OnlinePageController {

    @Autowired
    private OnlinePageService onlinePageService;
    @Autowired
    private OnlineFormService onlineFormService;
    @Autowired
    private OnlineDatasourceService onlineDatasourceService;

    /**
     * 新增在线表单页面数据。
     *
     * @param onlinePageDto 新增对象。
     * @return 应答结果对象，包含新增对象主键Id。
     */
    @OperationLog(type = SysOperationLogType.ADD)
    @PostMapping("/add")
    public ResponseResult<Long> add(@MyRequestBody OnlinePageDto onlinePageDto) {
        String errorMessage = MyCommonUtil.getModelValidationError(onlinePageDto);
        if (errorMessage != null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        OnlinePage onlinePage = MyModelUtil.copyTo(onlinePageDto, OnlinePage.class);
        if (onlinePageService.existByPageCode(onlinePage.getPageCode())) {
            errorMessage = "数据验证失败，页面编码已经存在！";
            return ResponseResult.error(ErrorCodeEnum.DUPLICATED_UNIQUE_KEY, errorMessage);
        }
        try {
            onlinePage = onlinePageService.saveNew(onlinePage);
        } catch (DuplicateKeyException e) {
            errorMessage = "数据验证失败，当前应用的页面编码已经存在！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        return ResponseResult.success(onlinePage.getPageId());
    }

    /**
     * 更新在线表单页面数据。
     *
     * @param onlinePageDto 更新对象。
     * @return 应答结果对象。
     */
    @OperationLog(type = SysOperationLogType.UPDATE)
    @PostMapping("/update")
    public ResponseResult<Void> update(@MyRequestBody OnlinePageDto onlinePageDto) {
        String errorMessage = MyCommonUtil.getModelValidationError(onlinePageDto, Default.class, UpdateGroup.class);
        if (errorMessage != null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        OnlinePage onlinePage = MyModelUtil.copyTo(onlinePageDto, OnlinePage.class);
        ResponseResult<OnlinePage> verifyResult = this.doVerifyAndGet(onlinePage.getPageId());
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        OnlinePage originalOnlinePage = verifyResult.getData();
        if (!onlinePage.getPageType().equals(originalOnlinePage.getPageType())) {
            errorMessage = "数据验证失败，页面类型不能修改！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        if (!StrUtil.equals(onlinePage.getPageCode(), originalOnlinePage.getPageCode())
                && onlinePageService.existByPageCode(onlinePage.getPageCode())) {
            errorMessage = "数据验证失败，页面编码已经存在！";
            return ResponseResult.error(ErrorCodeEnum.DUPLICATED_UNIQUE_KEY, errorMessage);
        }
        try {
            if (!onlinePageService.update(onlinePage, originalOnlinePage)) {
                return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
            }
        } catch (DuplicateKeyException e) {
            errorMessage = "数据验证失败，当前应用的页面编码已经存在！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        return ResponseResult.success();
    }

    /**
     * 更新在线表单页面对象的发布状态字段。
     *
     * @param pageId    待更新的页面对象主键Id。
     * @param published 发布状态。
     * @return 应答结果对象。
     */
    @OperationLog(type = SysOperationLogType.UPDATE)
    @PostMapping("/updatePublished")
    public ResponseResult<Void> updateStatus(
            @MyRequestBody(required = true) Long pageId,
            @MyRequestBody(required = true) Boolean published) {
        String errorMessage;
        ResponseResult<OnlinePage> verifyResult = this.doVerifyAndGet(pageId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        OnlinePage originalOnlinePage = verifyResult.getData();
        if (!published.equals(originalOnlinePage.getPublished())) {
            if (BooleanUtil.isTrue(published) && !originalOnlinePage.getStatus().equals(PageStatus.FORM_DESIGN)) {
                errorMessage = "数据验证失败，当前页面状态不为 [设计] 状态，因此不能发布！";
                return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
            }
            onlinePageService.updatePublished(pageId, published);
        }
        return ResponseResult.success();
    }

    /**
     * 删除在线表单页面数据。
     *
     * @param pageId 删除对象主键Id。
     * @return 应答结果对象。
     */
    @OperationLog(type = SysOperationLogType.DELETE)
    @PostMapping("/delete")
    public ResponseResult<Void> delete(@MyRequestBody Long pageId) {
        String errorMessage;
        ResponseResult<OnlinePage> verifyResult = this.doVerifyAndGet(pageId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        if (!onlinePageService.remove(pageId)) {
            errorMessage = "数据操作失败，删除的对象不存在，请刷新后重试！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        return ResponseResult.success();
    }

    /**
     * 列出符合过滤条件的在线表单页面列表。
     *
     * @param onlinePageDtoFilter 过滤对象。
     * @param orderParam          排序参数。
     * @param pageParam           分页参数。
     * @return 应答结果对象，包含查询结果集。
     */
    @PostMapping("/list")
    public ResponseResult<MyPageData<OnlinePageVo>> list(
            @MyRequestBody OnlinePageDto onlinePageDtoFilter,
            @MyRequestBody MyOrderParam orderParam,
            @MyRequestBody MyPageParam pageParam) {
        if (pageParam != null) {
            PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize());
        }
        OnlinePage onlinePageFilter = MyModelUtil.copyTo(onlinePageDtoFilter, OnlinePage.class);
        String orderBy = MyOrderParam.buildOrderBy(orderParam, OnlinePage.class);
        List<OnlinePage> onlinePageList = onlinePageService.getOnlinePageListWithRelation(onlinePageFilter, orderBy);
        return ResponseResult.success(MyPageUtil.makeResponseData(onlinePageList, OnlinePage.INSTANCE));
    }

    /**
     * 获取系统中配置的所有Page和表单的列表。
     *
     * @return 系统中配置的所有Page和表单的列表。
     */
    @PostMapping("/listAllPageAndForm")
    public ResponseResult<JSONObject> listAllPageAndForm() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("pageList", onlinePageService.getOnlinePageList(null, null));
        List<OnlineForm> formList = onlineFormService.getOnlineFormList(null, null);
        formList.forEach(f -> f.setWidgetJson(null));
        jsonObject.put("formList", formList);
        return ResponseResult.success(jsonObject);
    }

    /**
     * 查看指定在线表单页面对象详情。
     *
     * @param pageId 指定对象主键Id。
     * @return 应答结果对象，包含对象详情。
     */
    @GetMapping("/view")
    public ResponseResult<OnlinePageVo> view(@RequestParam Long pageId) {
        ResponseResult<OnlinePage> verifyResult = this.doVerifyAndGet(pageId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        OnlinePage onlinePage = onlinePageService.getByIdWithRelation(pageId, MyRelationParam.full());
        if (onlinePage == null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        OnlinePageVo onlinePageVo = OnlinePage.INSTANCE.fromModel(onlinePage);
        return ResponseResult.success(onlinePageVo);
    }

    /**
     * 列出与指定在线表单页面存在多对多关系的在线数据源列表数据。
     *
     * @param pageId                    主表关联字段。
     * @param onlineDatasourceDtoFilter 在线数据源过滤对象。
     * @param orderParam                排序参数。
     * @param pageParam                 分页参数。
     * @return 应答结果对象，返回符合条件的数据列表。
     */
    @PostMapping("/listOnlinePageDatasource")
    public ResponseResult<MyPageData<OnlineDatasourceVo>> listOnlinePageDatasource(
            @MyRequestBody Long pageId,
            @MyRequestBody OnlineDatasourceDto onlineDatasourceDtoFilter,
            @MyRequestBody MyOrderParam orderParam,
            @MyRequestBody MyPageParam pageParam) {
        ResponseResult<OnlinePage> verifyResult = this.doVerifyAndGet(pageId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        if (pageParam != null) {
            PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize());
        }
        OnlineDatasource filter = MyModelUtil.copyTo(onlineDatasourceDtoFilter, OnlineDatasource.class);
        String orderBy = MyOrderParam.buildOrderBy(orderParam, OnlineDatasource.class);
        List<OnlineDatasource> onlineDatasourceList =
                onlineDatasourceService.getOnlineDatasourceListByPageId(pageId, filter, orderBy);
        return ResponseResult.success(MyPageUtil.makeResponseData(onlineDatasourceList, OnlineDatasource.INSTANCE));
    }

    /**
     * 批量添加在线表单页面和在线数据源对象的多对多关联关系数据。
     *
     * @param pageId                      主表主键Id。
     * @param onlinePageDatasourceDtoList 关联对象列表。
     * @return 应答结果对象。
     */
    @OperationLog(type = SysOperationLogType.ADD_M2M)
    @PostMapping("/addOnlinePageDatasource")
    public ResponseResult<Void> addOnlinePageDatasource(
            @MyRequestBody Long pageId,
            @MyRequestBody(value = "onlinePageDatasourceList") List<OnlinePageDatasourceDto> onlinePageDatasourceDtoList) {
        String errorMessage;
        ResponseResult<OnlinePage> verifyResult = this.doVerifyAndGet(pageId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        if (MyCommonUtil.existBlankArgument(onlinePageDatasourceDtoList)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        for (OnlinePageDatasourceDto onlinePageDatasource : onlinePageDatasourceDtoList) {
            errorMessage = MyCommonUtil.getModelValidationError(onlinePageDatasource);
            if (errorMessage != null) {
                return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
            }
        }
        Set<Long> datasourceIdSet = onlinePageDatasourceDtoList.stream()
                .map(OnlinePageDatasourceDto::getDatasourceId).collect(Collectors.toSet());
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
        List<OnlinePageDatasource> onlinePageDatasourceList =
                MyModelUtil.copyCollectionTo(onlinePageDatasourceDtoList, OnlinePageDatasource.class);
        onlinePageService.addOnlinePageDatasourceList(onlinePageDatasourceList, pageId);
        return ResponseResult.success();
    }

    /**
     * 显示在线表单页面和指定数据源的多对多关联详情数据。
     *
     * @param pageId       主表主键Id。
     * @param datasourceId 从表主键Id。
     * @return 应答结果对象，包括中间表详情。
     */
    @GetMapping("/viewOnlinePageDatasource")
    public ResponseResult<OnlinePageDatasourceVo> viewOnlinePageDatasource(
            @RequestParam Long pageId, @RequestParam Long datasourceId) {
        ResponseResult<OnlinePage> verifyResult = this.doVerifyAndGet(pageId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        OnlinePageDatasource onlinePageDatasource = onlinePageService.getOnlinePageDatasource(pageId, datasourceId);
        if (onlinePageDatasource == null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        OnlinePageDatasourceVo onlinePageDatasourceVo =
                MyModelUtil.copyTo(onlinePageDatasource, OnlinePageDatasourceVo.class);
        return ResponseResult.success(onlinePageDatasourceVo);
    }

    /**
     * 移除指定在线表单页面和指定数据源的多对多关联关系。
     *
     * @param pageId       主表主键Id。
     * @param datasourceId 从表主键Id。
     * @return 应答结果对象。
     */
    @OperationLog(type = SysOperationLogType.DELETE_M2M)
    @PostMapping("/deleteOnlinePageDatasource")
    public ResponseResult<Void> deleteOnlinePageDatasource(
            @MyRequestBody Long pageId, @MyRequestBody(required = true) Long datasourceId) {
        ResponseResult<OnlinePage> verifyResult = this.doVerifyAndGet(pageId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        if (!onlinePageService.removeOnlinePageDatasource(pageId, datasourceId)) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        return ResponseResult.success();
    }

    private ResponseResult<OnlinePage> doVerifyAndGet(Long pageId) {
        String errorMessage;
        if (MyCommonUtil.existBlankArgument(pageId)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        OnlinePage onlinePage = onlinePageService.getById(pageId);
        if (onlinePage == null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        TokenData tokenData = TokenData.takeFromRequest();
        if (!StrUtil.equals(onlinePage.getAppCode(), tokenData.getAppCode())) {
            errorMessage = "数据验证失败，当前应用不存在该页面！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        if (ObjectUtil.notEqual(onlinePage.getTenantId(), tokenData.getTenantId())) {
            errorMessage = "数据验证失败，当前租户不包含该页面！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        return ResponseResult.success(onlinePage);
    }
}
