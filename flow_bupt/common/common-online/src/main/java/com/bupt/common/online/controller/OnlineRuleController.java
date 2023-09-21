package com.bupt.common.online.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.BooleanUtil;
import com.bupt.common.core.annotation.MyRequestBody;
import com.bupt.common.core.constant.ErrorCodeEnum;
import com.bupt.common.core.object.*;
import com.bupt.common.core.util.MyCommonUtil;
import com.bupt.common.core.util.MyModelUtil;
import com.bupt.common.core.util.MyPageUtil;
import com.bupt.common.core.validator.UpdateGroup;
import com.bupt.common.log.annotation.OperationLog;
import com.bupt.common.log.model.constant.SysOperationLogType;
import com.bupt.common.online.dto.OnlineRuleDto;
import com.bupt.common.online.model.OnlineRule;
import com.bupt.common.online.service.OnlineRuleService;
import com.bupt.common.online.vo.OnlineRuleVo;
import com.github.pagehelper.page.PageMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import javax.validation.groups.Default;
import java.util.List;

/**
 * 在线表单字段验证规则接口。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Slf4j
@RestController
@RequestMapping("${common-online.urlPrefix}/onlineRule")
@ConditionalOnProperty(name = "common-online.operationEnabled", havingValue = "true")
public class OnlineRuleController {

    @Autowired
    private OnlineRuleService onlineRuleService;

    /**
     * 新增验证规则数据。
     *
     * @param onlineRuleDto 新增对象。
     * @return 应答结果对象，包含新增对象主键Id。
     */
    @OperationLog(type = SysOperationLogType.ADD)
    @PostMapping("/add")
    public ResponseResult<Long> add(@MyRequestBody OnlineRuleDto onlineRuleDto) {
        String errorMessage = MyCommonUtil.getModelValidationError(onlineRuleDto);
        if (errorMessage != null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        OnlineRule onlineRule = MyModelUtil.copyTo(onlineRuleDto, OnlineRule.class);
        onlineRule = onlineRuleService.saveNew(onlineRule);
        return ResponseResult.success(onlineRule.getRuleId());
    }

    /**
     * 更新验证规则数据。
     *
     * @param onlineRuleDto 更新对象。
     * @return 应答结果对象。
     */
    @OperationLog(type = SysOperationLogType.UPDATE)
    @PostMapping("/update")
    public ResponseResult<Void> update(@MyRequestBody OnlineRuleDto onlineRuleDto) {
        String errorMessage = MyCommonUtil.getModelValidationError(onlineRuleDto, Default.class, UpdateGroup.class);
        if (errorMessage != null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        OnlineRule onlineRule = MyModelUtil.copyTo(onlineRuleDto, OnlineRule.class);
        ResponseResult<OnlineRule> verifyResult = this.doVerifyAndGet(onlineRule.getRuleId(), false);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        OnlineRule originalOnlineRule = verifyResult.getData();
        if (!onlineRuleService.update(onlineRule, originalOnlineRule)) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        return ResponseResult.success();
    }

    /**
     * 删除验证规则数据。
     *
     * @param ruleId 删除对象主键Id。
     * @return 应答结果对象。
     */
    @OperationLog(type = SysOperationLogType.DELETE)
    @PostMapping("/delete")
    public ResponseResult<Void> delete(@MyRequestBody Long ruleId) {
        String errorMessage;
        ResponseResult<OnlineRule> verifyResult = this.doVerifyAndGet(ruleId, false);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        if (!onlineRuleService.remove(ruleId)) {
            errorMessage = "数据操作失败，删除的对象不存在，请刷新后重试！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        return ResponseResult.success();
    }

    /**
     * 列出符合过滤条件的验证规则列表。
     *
     * @param onlineRuleDtoFilter 过滤对象。
     * @param orderParam          排序参数。
     * @param pageParam           分页参数。
     * @return 应答结果对象，包含查询结果集。
     */
    @PostMapping("/list")
    public ResponseResult<MyPageData<OnlineRuleVo>> list(
            @MyRequestBody OnlineRuleDto onlineRuleDtoFilter,
            @MyRequestBody MyOrderParam orderParam,
            @MyRequestBody MyPageParam pageParam) {
        if (pageParam != null) {
            PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize());
        }
        OnlineRule onlineRuleFilter = MyModelUtil.copyTo(onlineRuleDtoFilter, OnlineRule.class);
        String orderBy = MyOrderParam.buildOrderBy(orderParam, OnlineRule.class);
        List<OnlineRule> onlineRuleList = onlineRuleService.getOnlineRuleListWithRelation(onlineRuleFilter, orderBy);
        return ResponseResult.success(MyPageUtil.makeResponseData(onlineRuleList, OnlineRule.INSTANCE));
    }

    /**
     * 查看指定验证规则对象详情。
     *
     * @param ruleId 指定对象主键Id。
     * @return 应答结果对象，包含对象详情。
     */
    @GetMapping("/view")
    public ResponseResult<OnlineRuleVo> view(@RequestParam Long ruleId) {
        ResponseResult<OnlineRule> verifyResult = this.doVerifyAndGet(ruleId, true);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        OnlineRule onlineRule = verifyResult.getData();
        OnlineRuleVo onlineRuleVo = OnlineRule.INSTANCE.fromModel(onlineRule);
        return ResponseResult.success(onlineRuleVo);
    }

    private ResponseResult<OnlineRule> doVerifyAndGet(Long ruleId, boolean readOnly) {
        String errorMessage;
        if (MyCommonUtil.existBlankArgument(ruleId)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        // 验证关联Id的数据合法性
        OnlineRule rule = onlineRuleService.getById(ruleId);
        if (rule == null) {
            errorMessage = "数据验证失败，当前在线字段规则并不存在，请刷新后重试！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        if (!readOnly && BooleanUtil.isTrue(rule.getBuiltin())) {
            errorMessage = "数据验证失败，内置规则不能删除！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        if (!StrUtil.equals(rule.getAppCode(), TokenData.takeFromRequest().getAppCode())) {
            errorMessage = "数据验证失败，当前应用并不包含该规则！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        return ResponseResult.success(rule);
    }
}
