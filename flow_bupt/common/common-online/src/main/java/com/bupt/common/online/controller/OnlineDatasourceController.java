package com.bupt.common.online.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.bupt.common.core.annotation.MyRequestBody;
import com.bupt.common.core.constant.ErrorCodeEnum;
import com.bupt.common.core.object.*;
import com.bupt.common.core.util.MyCommonUtil;
import com.bupt.common.core.util.MyModelUtil;
import com.bupt.common.core.util.MyPageUtil;
import com.bupt.common.core.validator.AddGroup;
import com.bupt.common.core.validator.UpdateGroup;
import com.bupt.common.dbutil.object.SqlTable;
import com.bupt.common.dbutil.object.SqlTableColumn;
import com.bupt.common.log.annotation.OperationLog;
import com.bupt.common.log.model.constant.SysOperationLogType;
import com.bupt.common.online.dto.OnlineDatasourceDto;
import com.bupt.common.online.model.*;
import com.bupt.common.online.model.constant.PageType;
import com.bupt.common.online.service.*;
import com.bupt.common.online.vo.OnlineDatasourceVo;
import com.github.pagehelper.page.PageMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;

import javax.validation.groups.Default;
import java.util.List;

/**
 * 在线表单数据源接口。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Slf4j
@RestController
@RequestMapping("${common-online.urlPrefix}/onlineDatasource")
@ConditionalOnProperty(name = "common-online.operationEnabled", havingValue = "true")
public class OnlineDatasourceController {

    @Autowired
    private OnlineDatasourceService onlineDatasourceService;
    @Autowired
    private OnlineFormService onlineFormService;
    @Autowired
    private OnlinePageService onlinePageService;
    @Autowired
    private OnlineTableService onlineTableService;
    @Autowired
    private OnlineColumnService onlineColumnService;
    @Autowired
    private OnlineDblinkService onlineDblinkService;

    /**
     * 新增数据模型数据。
     *
     * @param onlineDatasourceDto 新增对象。
     * @param pageId              关联的页面Id。
     * @return 应答结果对象，包含新增对象主键Id。
     */
    @OperationLog(type = SysOperationLogType.ADD)
    @PostMapping("/add")
    public ResponseResult<Long> add(
            @MyRequestBody OnlineDatasourceDto onlineDatasourceDto,
            @MyRequestBody(required = true) Long pageId) {
        String errorMessage = MyCommonUtil.getModelValidationError(onlineDatasourceDto, Default.class, AddGroup.class);
        if (errorMessage != null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        OnlinePage onlinePage = onlinePageService.getById(pageId);
        if (onlinePage == null) {
            errorMessage = "数据验证失败，页面Id不存在！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        String appCode = TokenData.takeFromRequest().getAppCode();
        if (!StrUtil.equals(onlinePage.getAppCode(), appCode)) {
            errorMessage = "数据验证失败，当前应用并不存在该页面！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        OnlineDatasource onlineDatasource = MyModelUtil.copyTo(onlineDatasourceDto, OnlineDatasource.class);
        if (onlineDatasourceService.existByVariableName(onlineDatasource.getVariableName())) {
            errorMessage = "数据验证失败，当前数据源变量已经存在！";
            return ResponseResult.error(ErrorCodeEnum.DUPLICATED_UNIQUE_KEY, errorMessage);
        }
        OnlineDblink onlineDblink = onlineDblinkService.getById(onlineDatasourceDto.getDblinkId());
        if (onlineDblink == null) {
            errorMessage = "数据验证失败，关联的数据库链接Id不存在！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        if (!StrUtil.equals(onlineDblink.getAppCode(), appCode)) {
            errorMessage = "数据验证失败，当前应用并不存在该数据库链接！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        SqlTable sqlTable = onlineDblinkService.getDblinkTable(onlineDblink, onlineDatasourceDto.getMasterTableName());
        if (sqlTable == null) {
            errorMessage = "数据验证失败，指定的数据表名不存在！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        ResponseResult<Void> verifyResult = this.doVerifyPrimaryKey(sqlTable, onlinePage);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        try {
            onlineDatasource = onlineDatasourceService.saveNew(onlineDatasource, sqlTable, pageId);
        } catch (DuplicateKeyException e) {
            errorMessage = "数据验证失败，当前应用的数据源变量名已经存在！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        return ResponseResult.success(onlineDatasource.getDatasourceId());
    }

    /**
     * 更新数据模型数据。
     *
     * @param onlineDatasourceDto 更新对象。
     * @return 应答结果对象。
     */
    @OperationLog(type = SysOperationLogType.UPDATE)
    @PostMapping("/update")
    public ResponseResult<Void> update(@MyRequestBody OnlineDatasourceDto onlineDatasourceDto) {
        String errorMessage = MyCommonUtil.getModelValidationError(onlineDatasourceDto, Default.class, UpdateGroup.class);
        if (errorMessage != null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        OnlineDatasource onlineDatasource = MyModelUtil.copyTo(onlineDatasourceDto, OnlineDatasource.class);
        ResponseResult<OnlineDatasource> verifyResult = this.doVerifyAndGet(onlineDatasource.getDatasourceId());
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        OnlineDatasource originalOnlineDatasource = verifyResult.getData();
        if (!onlineDatasource.getDblinkId().equals(originalOnlineDatasource.getDblinkId())) {
            errorMessage = "数据验证失败，不能修改数据库链接Id！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        if (!onlineDatasource.getMasterTableId().equals(originalOnlineDatasource.getMasterTableId())) {
            errorMessage = "数据验证失败，不能修改主表Id！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        if (!StrUtil.equals(onlineDatasource.getVariableName(), originalOnlineDatasource.getVariableName())
                && onlineDatasourceService.existByVariableName(onlineDatasource.getVariableName())) {
            errorMessage = "数据验证失败，当前数据源变量已经存在！";
            return ResponseResult.error(ErrorCodeEnum.DUPLICATED_UNIQUE_KEY, errorMessage);
        }
        try {
            if (!onlineDatasourceService.update(onlineDatasource, originalOnlineDatasource)) {
                return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
            }
        } catch (DuplicateKeyException e) {
            errorMessage = "数据验证失败，当前应用的数据源变量名已经存在！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        return ResponseResult.success();
    }

    /**
     * 删除数据模型数据。
     *
     * @param datasourceId 删除对象主键Id。
     * @return 应答结果对象。
     */
    @OperationLog(type = SysOperationLogType.DELETE)
    @PostMapping("/delete")
    public ResponseResult<Void> delete(@MyRequestBody Long datasourceId) {
        String errorMessage;
        if (MyCommonUtil.existBlankArgument(datasourceId)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        ResponseResult<OnlineDatasource> verifyResult = this.doVerifyAndGet(datasourceId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        List<OnlineForm> formList = onlineFormService.getOnlineFormListByDatasourceId(datasourceId);
        if (CollUtil.isNotEmpty(formList)) {
            errorMessage = "数据验证失败，当前数据源正在被 [" + formList.get(0).getFormName() + "] 表单占用，请先删除关联数据！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        if (!onlineDatasourceService.remove(datasourceId)) {
            errorMessage = "数据操作失败，删除的对象不存在，请刷新后重试！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        return ResponseResult.success();
    }

    /**
     * 列出符合过滤条件的数据模型列表。
     *
     * @param onlineDatasourceDtoFilter 过滤对象。
     * @param orderParam                排序参数。
     * @param pageParam                 分页参数。
     * @return 应答结果对象，包含查询结果集。
     */
    @PostMapping("/list")
    public ResponseResult<MyPageData<OnlineDatasourceVo>> list(
            @MyRequestBody OnlineDatasourceDto onlineDatasourceDtoFilter,
            @MyRequestBody MyOrderParam orderParam,
            @MyRequestBody MyPageParam pageParam) {
        if (pageParam != null) {
            PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize());
        }
        OnlineDatasource onlineDatasourceFilter = MyModelUtil.copyTo(onlineDatasourceDtoFilter, OnlineDatasource.class);
        String orderBy = MyOrderParam.buildOrderBy(orderParam, OnlineDatasource.class);
        List<OnlineDatasource> onlineDatasourceList =
                onlineDatasourceService.getOnlineDatasourceListWithRelation(onlineDatasourceFilter, orderBy);
        return ResponseResult.success(MyPageUtil.makeResponseData(onlineDatasourceList, OnlineDatasource.INSTANCE));
    }

    /**
     * 查看指定数据模型对象详情。
     *
     * @param datasourceId 指定对象主键Id。
     * @return 应答结果对象，包含对象详情。
     */
    @GetMapping("/view")
    public ResponseResult<OnlineDatasourceVo> view(@RequestParam Long datasourceId) {
        ResponseResult<OnlineDatasource> verifyResult = this.doVerifyAndGet(datasourceId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        OnlineDatasource onlineDatasource =
                onlineDatasourceService.getByIdWithRelation(datasourceId, MyRelationParam.full());
        OnlineDatasourceVo onlineDatasourceVo = OnlineDatasource.INSTANCE.fromModel(onlineDatasource);
        List<OnlineTable> tableList = onlineTableService.getOnlineTableListByDatasourceId(datasourceId);
        if (CollUtil.isNotEmpty(tableList)) {
            onlineDatasourceVo.setTableList(OnlineTable.INSTANCE.fromModelList(tableList));
        }
        return ResponseResult.success(onlineDatasourceVo);
    }

    private ResponseResult<OnlineDatasource> doVerifyAndGet(Long datasourceId) {
        if (MyCommonUtil.existBlankArgument(datasourceId)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        OnlineDatasource onlineDatasource = onlineDatasourceService.getById(datasourceId);
        if (onlineDatasource == null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        if (!StrUtil.equals(onlineDatasource.getAppCode(), TokenData.takeFromRequest().getAppCode())) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, "数据验证失败，当前应用并不存在该数据源！");
        }
        return ResponseResult.success(onlineDatasource);
    }

    private ResponseResult<Void> doVerifyPrimaryKey(SqlTable sqlTable, OnlinePage onlinePage) {
        String errorMessage;
        boolean hasPrimaryKey = false;
        for (SqlTableColumn tableColumn : sqlTable.getColumnList()) {
            if (BooleanUtil.isFalse(tableColumn.getPrimaryKey())) {
                continue;
            }
            if (hasPrimaryKey) {
                errorMessage = "数据验证失败，数据表只能包含一个主键字段！";
                return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
            }
            hasPrimaryKey = true;
            // 流程表单的主表主键，不能是自增主键。
            if (BooleanUtil.isTrue(tableColumn.getAutoIncrement())
                    && onlinePage.getPageType().equals(PageType.FLOW)) {
                errorMessage = "数据验证失败，流程页面所关联的主表主键，不能是自增主键！";
                return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
            }
            CallResult verifyResult = onlineColumnService.verifyPrimaryKey(tableColumn);
            if (!verifyResult.isSuccess()) {
                return ResponseResult.errorFrom(verifyResult);
            }
        }
        if (!hasPrimaryKey) {
            errorMessage = "数据验证失败，数据表必须包含主键字段！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        return ResponseResult.success();
    }
}
