package com.bupt.common.flow.controller;

import com.github.pagehelper.page.PageMethod;
import com.bupt.common.flow.vo.*;
import com.bupt.common.flow.dto.*;
import com.bupt.common.flow.model.*;
import com.bupt.common.flow.service.*;
import com.bupt.common.core.object.*;
import com.bupt.common.core.util.*;
import com.bupt.common.core.constant.*;
import com.bupt.common.core.annotation.MyRequestBody;
import com.bupt.common.core.validator.UpdateGroup;
import com.bupt.common.log.annotation.OperationLog;
import com.bupt.common.log.model.constant.SysOperationLogType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import javax.validation.groups.Default;

/**
 * 工作流流程变量接口。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Slf4j
@RestController
@RequestMapping("${common-flow.urlPrefix}/flowEntryVariable")
@ConditionalOnProperty(name = "common-flow.operationEnabled", havingValue = "true")
public class FlowEntryVariableController {

    @Autowired
    private FlowEntryVariableService flowEntryVariableService;

    /**
     * 新增流程变量数据。
     *
     * @param flowEntryVariableDto 新增对象。
     * @return 应答结果对象，包含新增对象主键Id。
     */
    @OperationLog(type = SysOperationLogType.ADD)
    @PostMapping("/add")
    public ResponseResult<Long> add(@MyRequestBody FlowEntryVariableDto flowEntryVariableDto) {
        String errorMessage = MyCommonUtil.getModelValidationError(flowEntryVariableDto);
        if (errorMessage != null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        FlowEntryVariable flowEntryVariable = MyModelUtil.copyTo(flowEntryVariableDto, FlowEntryVariable.class);
        flowEntryVariable = flowEntryVariableService.saveNew(flowEntryVariable);
        return ResponseResult.success(flowEntryVariable.getVariableId());
    }

    /**
     * 更新流程变量数据。
     *
     * @param flowEntryVariableDto 更新对象。
     * @return 应答结果对象。
     */
    @OperationLog(type = SysOperationLogType.UPDATE)
    @PostMapping("/update")
    public ResponseResult<Void> update(@MyRequestBody FlowEntryVariableDto flowEntryVariableDto) {
        String errorMessage = MyCommonUtil.getModelValidationError(flowEntryVariableDto, Default.class, UpdateGroup.class);
        if (errorMessage != null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        FlowEntryVariable flowEntryVariable = MyModelUtil.copyTo(flowEntryVariableDto, FlowEntryVariable.class);
        FlowEntryVariable originalFlowEntryVariable = flowEntryVariableService.getById(flowEntryVariable.getVariableId());
        if (originalFlowEntryVariable == null) {
            // NOTE: 修改下面方括号中的话述
            errorMessage = "数据验证失败，当前 [数据] 并不存在，请刷新后重试！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        if (!flowEntryVariableService.update(flowEntryVariable, originalFlowEntryVariable)) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        return ResponseResult.success();
    }

    /**
     * 删除流程变量数据。
     *
     * @param variableId 删除对象主键Id。
     * @return 应答结果对象。
     */
    @OperationLog(type = SysOperationLogType.DELETE)
    @PostMapping("/delete")
    public ResponseResult<Void> delete(@MyRequestBody Long variableId) {
        String errorMessage;
        if (MyCommonUtil.existBlankArgument(variableId)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        // 验证关联Id的数据合法性
        FlowEntryVariable originalFlowEntryVariable = flowEntryVariableService.getById(variableId);
        if (originalFlowEntryVariable == null) {
            // NOTE: 修改下面方括号中的话述
            errorMessage = "数据验证失败，当前 [对象] 并不存在，请刷新后重试！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        if (!flowEntryVariableService.remove(variableId)) {
            errorMessage = "数据操作失败，删除的对象不存在，请刷新后重试！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        return ResponseResult.success();
    }

    /**
     * 列出符合过滤条件的流程变量列表。
     *
     * @param flowEntryVariableDtoFilter 过滤对象。
     * @param orderParam 排序参数。
     * @param pageParam 分页参数。
     * @return 应答结果对象，包含查询结果集。
     */
    @PostMapping("/list")
    public ResponseResult<MyPageData<FlowEntryVariableVo>> list(
            @MyRequestBody FlowEntryVariableDto flowEntryVariableDtoFilter,
            @MyRequestBody MyOrderParam orderParam,
            @MyRequestBody MyPageParam pageParam) {
        if (pageParam != null) {
            PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize());
        }
        FlowEntryVariable flowEntryVariableFilter = MyModelUtil.copyTo(flowEntryVariableDtoFilter, FlowEntryVariable.class);
        String orderBy = MyOrderParam.buildOrderBy(orderParam, FlowEntryVariable.class);
        List<FlowEntryVariable> flowEntryVariableList =
                flowEntryVariableService.getFlowEntryVariableListWithRelation(flowEntryVariableFilter, orderBy);
        return ResponseResult.success(MyPageUtil.makeResponseData(flowEntryVariableList, FlowEntryVariable.INSTANCE));
    }

    /**
     * 查看指定流程变量对象详情。
     *
     * @param variableId 指定对象主键Id。
     * @return 应答结果对象，包含对象详情。
     */
    @GetMapping("/view")
    public ResponseResult<FlowEntryVariableVo> view(@RequestParam Long variableId) {
        if (MyCommonUtil.existBlankArgument(variableId)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        FlowEntryVariable flowEntryVariable = flowEntryVariableService.getByIdWithRelation(variableId, MyRelationParam.full());
        if (flowEntryVariable == null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        FlowEntryVariableVo flowEntryVariableVo = FlowEntryVariable.INSTANCE.fromModel(flowEntryVariable);
        return ResponseResult.success(flowEntryVariableVo);
    }
}
