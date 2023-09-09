package com.bupt.common.flow.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.page.PageMethod;
import com.bupt.common.core.annotation.MyRequestBody;
import com.bupt.common.core.constant.ErrorCodeEnum;
import com.bupt.common.core.object.*;
import com.bupt.common.core.util.MyCommonUtil;
import com.bupt.common.core.util.MyModelUtil;
import com.bupt.common.core.util.MyPageUtil;
import com.bupt.common.core.validator.UpdateGroup;
import com.bupt.common.log.annotation.OperationLog;
import com.bupt.common.log.model.constant.SysOperationLogType;
import com.bupt.common.flow.dto.*;
import com.bupt.common.flow.model.*;
import com.bupt.common.flow.model.constant.FlowEntryStatus;
import com.bupt.common.flow.service.*;
import com.bupt.common.flow.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import javax.validation.groups.Default;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * 工作流流程分类接口。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Slf4j
@RestController
@RequestMapping("${common-flow.urlPrefix}/flowCategory")
@ConditionalOnProperty(name = "common-flow.operationEnabled", havingValue = "true")
public class FlowCategoryController {

    @Autowired
    private FlowCategoryService flowCategoryService;
    @Autowired
    private FlowEntryService flowEntryService;

    /**
     * 新增FlowCategory数据。
     *
     * @param flowCategoryDto 新增对象。
     * @return 应答结果对象，包含新增对象主键Id。
     */
    @OperationLog(type = SysOperationLogType.ADD)
    @PostMapping("/add")
    public ResponseResult<Long> add(@MyRequestBody FlowCategoryDto flowCategoryDto) {
        String errorMessage = MyCommonUtil.getModelValidationError(flowCategoryDto);
        if (errorMessage != null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        FlowCategory flowCategory = MyModelUtil.copyTo(flowCategoryDto, FlowCategory.class);
        if (flowCategoryService.existByCode(flowCategory.getCode())) {
            return ResponseResult.error(ErrorCodeEnum.DUPLICATED_UNIQUE_KEY, "数据验证失败，当前流程分类已经存在！");
        }
        flowCategory = flowCategoryService.saveNew(flowCategory);
        return ResponseResult.success(flowCategory.getCategoryId());
    }

    /**
     * 更新FlowCategory数据。
     *
     * @param flowCategoryDto 更新对象。
     * @return 应答结果对象。
     */
    @OperationLog(type = SysOperationLogType.UPDATE)
    @PostMapping("/update")
    public ResponseResult<Void> update(@MyRequestBody FlowCategoryDto flowCategoryDto) {
        String errorMessage = MyCommonUtil.getModelValidationError(flowCategoryDto, Default.class, UpdateGroup.class);
        if (errorMessage != null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        FlowCategory flowCategory = MyModelUtil.copyTo(flowCategoryDto, FlowCategory.class);
        ResponseResult<FlowCategory> verifyResult = this.doVerifyAndGet(flowCategory.getCategoryId());
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        FlowCategory originalFlowCategory = verifyResult.getData();
        if (!StrUtil.equals(flowCategory.getCode(), originalFlowCategory.getCode())) {
            FlowEntry filter = new FlowEntry();
            filter.setCategoryId(flowCategory.getCategoryId());
            filter.setStatus(FlowEntryStatus.PUBLISHED);
            List<FlowEntry> flowEntryList = flowEntryService.getListByFilter(filter);
            if (CollUtil.isNotEmpty(flowEntryList)) {
                errorMessage = "数据验证失败，当前流程分类存在已经发布的流程数据，因此分类标识不能修改！";
                return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
            }
            if (flowCategoryService.existByCode(flowCategory.getCode())) {
                errorMessage = "数据验证失败，当前流程分类已经存在！";
                return ResponseResult.error(ErrorCodeEnum.DUPLICATED_UNIQUE_KEY, errorMessage);
            }
        }
        if (!flowCategoryService.update(flowCategory, originalFlowCategory)) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        return ResponseResult.success();
    }

    /**
     * 删除FlowCategory数据。
     *
     * @param categoryId 删除对象主键Id。
     * @return 应答结果对象。
     */
    @OperationLog(type = SysOperationLogType.DELETE)
    @PostMapping("/delete")
    public ResponseResult<Void> delete(@MyRequestBody Long categoryId) {
        String errorMessage;
        ResponseResult<FlowCategory> verifyResult = this.doVerifyAndGet(categoryId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        FlowEntry filter = new FlowEntry();
        filter.setCategoryId(categoryId);
        List<FlowEntry> flowEntryList = flowEntryService.getListByFilter(filter);
        if (CollUtil.isNotEmpty(flowEntryList)) {
            errorMessage = "数据验证失败，请先删除当前流程分类关联的流程数据！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        if (!flowCategoryService.remove(categoryId)) {
            errorMessage = "数据操作失败，删除的对象不存在，请刷新后重试！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        return ResponseResult.success();
    }

    /**
     * 列出符合过滤条件的FlowCategory列表。
     *
     * @param flowCategoryDtoFilter 过滤对象。
     * @param orderParam 排序参数。
     * @param pageParam 分页参数。
     * @return 应答结果对象，包含查询结果集。
     */
    @PostMapping("/list")
    public ResponseResult<MyPageData<FlowCategoryVo>> list(
            @MyRequestBody FlowCategoryDto flowCategoryDtoFilter,
            @MyRequestBody MyOrderParam orderParam,
            @MyRequestBody MyPageParam pageParam) {
        if (pageParam != null) {
            PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize());
        }
        FlowCategory flowCategoryFilter = MyModelUtil.copyTo(flowCategoryDtoFilter, FlowCategory.class);
        String orderBy = MyOrderParam.buildOrderBy(orderParam, FlowCategory.class);
        List<FlowCategory> flowCategoryList = flowCategoryService.getFlowCategoryListWithRelation(flowCategoryFilter, orderBy);
        return ResponseResult.success(MyPageUtil.makeResponseData(flowCategoryList, FlowCategory.INSTANCE));
    }

    /**
     * 查看指定FlowCategory对象详情。
     *
     * @param categoryId 指定对象主键Id。
     * @return 应答结果对象，包含对象详情。
     */
    @GetMapping("/view")
    public ResponseResult<FlowCategoryVo> view(@RequestParam Long categoryId) {
        ResponseResult<FlowCategory> verifyResult = this.doVerifyAndGet(categoryId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        FlowCategoryVo flowCategoryVo = FlowCategory.INSTANCE.fromModel(verifyResult.getData());
        return ResponseResult.success(flowCategoryVo);
    }

    /**
     * 以字典形式返回全部FlowCategory数据集合。字典的键值为[categoryId, name]。
     * 白名单接口，登录用户均可访问。
     *
     * @param filter 过滤对象。
     * @return 应答结果对象，包含的数据为 List<Map<String, String>>，map中包含两条记录，key的值分别是id和name，value对应具体数据。
     */
    @GetMapping("/listDict")
    public ResponseResult<List<Map<String, Object>>> listDict(FlowCategory filter) {
        List<FlowCategory> resultList = flowCategoryService.getFlowCategoryList(filter, null);
        return ResponseResult.success(
                MyCommonUtil.toDictDataList(resultList, FlowCategory::getCategoryId, FlowCategory::getName));
    }

    /**
     * 根据字典Id集合，获取查询后的字典数据。
     *
     * @param dictIds 字典Id集合。
     * @return 应答结果对象，包含字典形式的数据集合。
     */
    @PostMapping("/listDictByIds")
    public ResponseResult<List<Map<String, Object>>> listDictByIds(@MyRequestBody List<Long> dictIds) {
        List<FlowCategory> resultList = flowCategoryService.getInList(new HashSet<>(dictIds));
        return ResponseResult.success(
                MyCommonUtil.toDictDataList(resultList, FlowCategory::getCategoryId, FlowCategory::getName));
    }

    private ResponseResult<FlowCategory> doVerifyAndGet(Long categoryId) {
        String errorMessage;
        if (MyCommonUtil.existBlankArgument(categoryId)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        FlowCategory flowCategory = flowCategoryService.getById(categoryId);
        if (flowCategory == null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        TokenData tokenData = TokenData.takeFromRequest();
        if (!StrUtil.equals(flowCategory.getAppCode(), tokenData.getAppCode())) {
            errorMessage = "数据验证失败，当前应用并不存在该流程分类的定义！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        if (ObjectUtil.notEqual(flowCategory.getTenantId(), tokenData.getTenantId())) {
            errorMessage = "数据验证失败，当前租户并不存在该流程分类的定义！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        return ResponseResult.success(flowCategory);
    }
}
