package com.bupt.common.online.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.bupt.common.core.annotation.MyRequestBody;
import com.bupt.common.core.constant.ErrorCodeEnum;
import com.bupt.common.core.object.*;
import com.bupt.common.core.util.MyCommonUtil;
import com.bupt.common.core.util.MyModelUtil;
import com.bupt.common.core.util.MyPageUtil;
import com.bupt.common.core.validator.UpdateGroup;
import com.bupt.common.dict.dto.GlobalDictDto;
import com.bupt.common.dict.util.GlobalDictOperationHelper;
import com.bupt.common.dict.vo.GlobalDictVo;
import com.bupt.common.log.annotation.OperationLog;
import com.bupt.common.log.model.constant.SysOperationLogType;
import com.bupt.common.online.dto.OnlineDictDto;
import com.bupt.common.online.model.OnlineColumn;
import com.bupt.common.online.model.OnlineDict;
import com.bupt.common.online.model.OnlineTable;
import com.bupt.common.online.service.OnlineColumnService;
import com.bupt.common.online.service.OnlineTableService;
import com.bupt.common.online.service.OnlineDictService;
import com.bupt.common.online.vo.OnlineDictVo;
import com.github.pagehelper.page.PageMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import javax.validation.groups.Default;
import java.util.List;

/**
 * 在线表单字典接口。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Slf4j
@RestController
@RequestMapping("${common-online.urlPrefix}/onlineDict")
@ConditionalOnProperty(name = "common-online.operationEnabled", havingValue = "true")
public class OnlineDictController {

    @Autowired
    private OnlineDictService onlineDictService;
    @Autowired
    private OnlineColumnService onlineColumnService;
    @Autowired
    private OnlineTableService onlineTableService;
    @Autowired
    private GlobalDictOperationHelper globalDictOperationHelper;

    /**
     * 新增在线表单字典数据。
     *
     * @param onlineDictDto 新增对象。
     * @return 应答结果对象，包含新增对象主键Id。
     */
    @OperationLog(type = SysOperationLogType.ADD)
    @PostMapping("/add")
    public ResponseResult<Long> add(@MyRequestBody OnlineDictDto onlineDictDto) {
        String errorMessage = MyCommonUtil.getModelValidationError(onlineDictDto);
        if (errorMessage != null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        OnlineDict onlineDict = MyModelUtil.copyTo(onlineDictDto, OnlineDict.class);
        // 验证关联Id的数据合法性
        CallResult callResult = onlineDictService.verifyRelatedData(onlineDict, null);
        if (!callResult.isSuccess()) {
            errorMessage = callResult.getErrorMessage();
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        onlineDict = onlineDictService.saveNew(onlineDict);
        return ResponseResult.success(onlineDict.getDictId());
    }

    /**
     * 更新在线表单字典数据。
     *
     * @param onlineDictDto 更新对象。
     * @return 应答结果对象。
     */
    @OperationLog(type = SysOperationLogType.UPDATE)
    @PostMapping("/update")
    public ResponseResult<Void> update(@MyRequestBody OnlineDictDto onlineDictDto) {
        String errorMessage = MyCommonUtil.getModelValidationError(onlineDictDto, Default.class, UpdateGroup.class);
        if (errorMessage != null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        OnlineDict onlineDict = MyModelUtil.copyTo(onlineDictDto, OnlineDict.class);
        ResponseResult<OnlineDict> verifyResult = this.doVerifyAndGet(onlineDict.getDictId());
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        OnlineDict originalOnlineDict = verifyResult.getData();
        // 验证关联Id的数据合法性
        CallResult callResult = onlineDictService.verifyRelatedData(onlineDict, originalOnlineDict);
        if (!callResult.isSuccess()) {
            errorMessage = callResult.getErrorMessage();
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        if (!onlineDictService.update(onlineDict, originalOnlineDict)) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        return ResponseResult.success();
    }

    /**
     * 删除在线表单字典数据。
     *
     * @param dictId 删除对象主键Id。
     * @return 应答结果对象。
     */
    @OperationLog(type = SysOperationLogType.DELETE)
    @PostMapping("/delete")
    public ResponseResult<Void> delete(@MyRequestBody Long dictId) {
        String errorMessage;
        ResponseResult<OnlineDict> verifyResult = this.doVerifyAndGet(dictId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        OnlineColumn filter = new OnlineColumn();
        filter.setDictId(dictId);
        List<OnlineColumn> columns = onlineColumnService.getListByFilter(filter);
        if (CollUtil.isNotEmpty(columns)) {
            OnlineColumn usingColumn = columns.get(0);
            OnlineTable table = onlineTableService.getById(usingColumn.getTableId());
            errorMessage = String.format("数据验证失败，数据表 [%s] 字段 [%s] 正在引用该字典，因此不能直接删除！",
                    table.getTableName(), usingColumn.getColumnName());
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        if (!onlineDictService.remove(dictId)) {
            errorMessage = "数据操作失败，删除的对象不存在，请刷新后重试！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        return ResponseResult.success();
    }

    /**
     * 列出符合过滤条件的在线表单字典列表。
     *
     * @param onlineDictDtoFilter 过滤对象。
     * @param orderParam          排序参数。
     * @param pageParam           分页参数。
     * @return 应答结果对象，包含查询结果集。
     */
    @PostMapping("/list")
    public ResponseResult<MyPageData<OnlineDictVo>> list(
            @MyRequestBody OnlineDictDto onlineDictDtoFilter,
            @MyRequestBody MyOrderParam orderParam,
            @MyRequestBody MyPageParam pageParam) {
        if (pageParam != null) {
            PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize());
        }
        OnlineDict onlineDictFilter = MyModelUtil.copyTo(onlineDictDtoFilter, OnlineDict.class);
        String orderBy = MyOrderParam.buildOrderBy(orderParam, OnlineDict.class);
        List<OnlineDict> onlineDictList = onlineDictService.getOnlineDictListWithRelation(onlineDictFilter, orderBy);
        return ResponseResult.success(MyPageUtil.makeResponseData(onlineDictList, OnlineDict.INSTANCE));
    }

    /**
     * 查看指定在线表单字典对象详情。
     *
     * @param dictId 指定对象主键Id。
     * @return 应答结果对象，包含对象详情。
     */
    @GetMapping("/view")
    public ResponseResult<OnlineDictVo> view(@RequestParam Long dictId) {
        ResponseResult<OnlineDict> verifyResult = this.doVerifyAndGet(dictId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        OnlineDict onlineDict = onlineDictService.getByIdWithRelation(dictId, MyRelationParam.full());
        if (onlineDict == null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        OnlineDictVo onlineDictVo = OnlineDict.INSTANCE.fromModel(onlineDict);
        return ResponseResult.success(onlineDictVo);
    }

    /**
     * 获取全部编码字典列表。
     * NOTE: 白名单接口。
     *
     * @param globalDictDtoFilter 过滤对象。
     * @param pageParam           分页参数。
     * @return 字典的数据列表。
     */
    @PostMapping("/listAllGlobalDict")
    public ResponseResult<MyPageData<GlobalDictVo>> listAllGlobalDict(
            @MyRequestBody GlobalDictDto globalDictDtoFilter,
            @MyRequestBody MyPageParam pageParam) {
        return globalDictOperationHelper.listAllGlobalDict(globalDictDtoFilter, pageParam);
    }

    private ResponseResult<OnlineDict> doVerifyAndGet(Long dictId) {
        if (MyCommonUtil.existBlankArgument(dictId)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        OnlineDict originalDict = onlineDictService.getById(dictId);
        if (originalDict == null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        if (!StrUtil.equals(originalDict.getAppCode(), TokenData.takeFromRequest().getAppCode())) {
            return ResponseResult.error(
                    ErrorCodeEnum.DATA_VALIDATED_FAILED, "数据验证失败，当前应用不存在该在线表单字典！");
        }
        return ResponseResult.success(originalDict);
    }
}
