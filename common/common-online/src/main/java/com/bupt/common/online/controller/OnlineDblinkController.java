package com.bupt.common.online.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bupt.common.core.annotation.MyRequestBody;
import com.bupt.common.core.constant.ErrorCodeEnum;
import com.bupt.common.core.object.*;
import com.bupt.common.core.util.MyCommonUtil;
import com.bupt.common.core.util.MyModelUtil;
import com.bupt.common.core.util.MyPageUtil;
import com.bupt.common.dbutil.object.SqlTable;
import com.bupt.common.dbutil.object.SqlTableColumn;
import com.bupt.common.log.annotation.OperationLog;
import com.bupt.common.log.model.constant.SysOperationLogType;
import com.bupt.common.online.dto.OnlineDblinkDto;
import com.bupt.common.online.model.OnlineDblink;
import com.bupt.common.online.service.OnlineDblinkService;
import com.bupt.common.online.util.OnlineDataSourceUtil;
import com.bupt.common.online.vo.OnlineDblinkVo;
import com.github.pagehelper.page.PageMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 在线表单数据库链接接口。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Slf4j
@RestController
@RequestMapping("${common-online.urlPrefix}/onlineDblink")
@ConditionalOnProperty(name = "common-online.operationEnabled", havingValue = "true")
public class OnlineDblinkController {

    @Autowired
    private OnlineDblinkService onlineDblinkService;
    @Autowired
    private OnlineDataSourceUtil dataSourceUtil;

    /**
     * 新增数据库链接数据。
     *
     * @param onlineDblinkDto 新增对象。
     * @return 应答结果对象，包含新增对象主键Id。
     */
    @OperationLog(type = SysOperationLogType.ADD)
    @PostMapping("/add")
    public ResponseResult<Long> add(@MyRequestBody OnlineDblinkDto onlineDblinkDto) {
        String errorMessage = MyCommonUtil.getModelValidationError(onlineDblinkDto, false);
        if (errorMessage != null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        OnlineDblink onlineDblink = MyModelUtil.copyTo(onlineDblinkDto, OnlineDblink.class);
        onlineDblink = onlineDblinkService.saveNew(onlineDblink);
        return ResponseResult.success(onlineDblink.getDblinkId());
    }

    /**
     * 更新数据库链接数据。
     *
     * @param onlineDblinkDto 更新对象。
     * @return 应答结果对象。
     */
    @OperationLog(type = SysOperationLogType.UPDATE)
    @PostMapping("/update")
    public ResponseResult<Void> update(@MyRequestBody OnlineDblinkDto onlineDblinkDto) {
        String errorMessage = MyCommonUtil.getModelValidationError(onlineDblinkDto, true);
        if (errorMessage != null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        OnlineDblink onlineDblink = MyModelUtil.copyTo(onlineDblinkDto, OnlineDblink.class);
        ResponseResult<OnlineDblink> verifyResult = this.doVerifyAndGet(onlineDblinkDto.getDblinkId());
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        OnlineDblink originalOnlineDblink = verifyResult.getData();
        if (ObjectUtil.notEqual(onlineDblink.getDblinkType(), originalOnlineDblink.getDblinkType())) {
            errorMessage = "数据验证失败，不能修改数据库类型！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        String passwdKey = "password";
        JSONObject configJson = JSON.parseObject(onlineDblink.getConfiguration());
        String password = configJson.getString(passwdKey);
        if (StrUtil.isNotBlank(password) && StrUtil.isAllCharMatch(password, c -> '*' == c)) {
            password = JSON.parseObject(originalOnlineDblink.getConfiguration()).getString(passwdKey);
            configJson.put(passwdKey, password);
            onlineDblink.setConfiguration(configJson.toJSONString());
        }
        if (!onlineDblinkService.update(onlineDblink, originalOnlineDblink)) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        return ResponseResult.success();
    }

    /**
     * 删除数据库链接数据。
     *
     * @param dblinkId 删除对象主键Id。
     * @return 应答结果对象。
     */
    @OperationLog(type = SysOperationLogType.DELETE)
    @PostMapping("/delete")
    public ResponseResult<Void> delete(@MyRequestBody Long dblinkId) {
        String errorMessage;
        // 验证关联Id的数据合法性
        ResponseResult<OnlineDblink> verifyResult = this.doVerifyAndGet(dblinkId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        if (!onlineDblinkService.remove(dblinkId)) {
            errorMessage = "数据操作失败，删除的对象不存在，请刷新后重试！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        return ResponseResult.success();
    }

    /**
     * 列出符合过滤条件的数据库链接列表。
     *
     * @param onlineDblinkDtoFilter 过滤对象。
     * @param orderParam            排序参数。
     * @param pageParam             分页参数。
     * @return 应答结果对象，包含查询结果集。
     */
    @PostMapping("/list")
    public ResponseResult<MyPageData<OnlineDblinkVo>> list(
            @MyRequestBody OnlineDblinkDto onlineDblinkDtoFilter,
            @MyRequestBody MyOrderParam orderParam,
            @MyRequestBody MyPageParam pageParam) {
        if (pageParam != null) {
            PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize());
        }
        OnlineDblink onlineDblinkFilter = MyModelUtil.copyTo(onlineDblinkDtoFilter, OnlineDblink.class);
        String orderBy = MyOrderParam.buildOrderBy(orderParam, OnlineDblink.class);
        List<OnlineDblink> onlineDblinkList =
                onlineDblinkService.getOnlineDblinkListWithRelation(onlineDblinkFilter, orderBy);
        for (OnlineDblink dblink : onlineDblinkList) {
            this.maskOffPassword(dblink);
        }
        return ResponseResult.success(MyPageUtil.makeResponseData(onlineDblinkList, OnlineDblink.INSTANCE));
    }

    /**
     * 查看指定数据库链接对象详情。
     *
     * @param dblinkId 指定对象主键Id。
     * @return 应答结果对象，包含对象详情。
     */
    @GetMapping("/view")
    public ResponseResult<OnlineDblinkVo> view(@RequestParam Long dblinkId) {
        ResponseResult<OnlineDblink> verifyResult = this.doVerifyAndGet(dblinkId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        OnlineDblink onlineDblink = verifyResult.getData();
        onlineDblinkService.buildRelationForData(onlineDblink, MyRelationParam.full());
        if (!StrUtil.equals(onlineDblink.getAppCode(), TokenData.takeFromRequest().getAppCode())) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, "数据验证失败，当前应用并不存在该数据库链接！");
        }
        this.maskOffPassword(onlineDblink);
        OnlineDblinkVo onlineDblinkVo = OnlineDblink.INSTANCE.fromModel(onlineDblink);
        return ResponseResult.success(onlineDblinkVo);
    }

    /**
     * 获取指定数据库链接下的所有动态表单依赖的数据表列表。
     *
     * @param dblinkId 数据库链接Id。
     * @return 所有动态表单依赖的数据表列表
     */
    @GetMapping("/listDblinkTables")
    public ResponseResult<List<SqlTable>> listDblinkTables(@RequestParam Long dblinkId) {
        OnlineDblink dblink = onlineDblinkService.getById(dblinkId);
        if (dblink == null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        return ResponseResult.success(onlineDblinkService.getDblinkTableList(dblink));
    }

    /**
     * 获取指定数据库链接下，指定数据表的所有字段信息。
     *
     * @param dblinkId  数据库链接Id。
     * @param tableName 表名。
     * @return 该表的所有字段列表。
     */
    @GetMapping("/listDblinkTableColumns")
    public ResponseResult<List<SqlTableColumn>> listDblinkTableColumns(
            @RequestParam Long dblinkId, @RequestParam String tableName) {
        OnlineDblink dblink = onlineDblinkService.getById(dblinkId);
        if (dblink == null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        return ResponseResult.success(onlineDblinkService.getDblinkTableColumnList(dblink, tableName));
    }

    /**
     * 测试数据库链接的接口。
     *
     * @return 应答结果。
     */
    @GetMapping("/testConnection")
    public ResponseResult<Void> testConnection(@RequestParam Long dblinkId) {
        ResponseResult<OnlineDblink> verifyAndGet = this.doVerifyAndGet(dblinkId);
        if (!verifyAndGet.isSuccess()) {
            return ResponseResult.errorFrom(verifyAndGet);
        }
        try {
            dataSourceUtil.testConnection(dblinkId);
            return ResponseResult.success();
        } catch (Exception e) {
            log.error("Failed to test connection with ONLINE_DBLINK_ID [" + dblinkId + "]!", e);
            return ResponseResult.error(ErrorCodeEnum.DATA_ACCESS_FAILED, "数据库连接失败！");
        }
    }

    private ResponseResult<OnlineDblink> doVerifyAndGet(Long dblinkId) {
        if (MyCommonUtil.existBlankArgument(dblinkId)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        OnlineDblink onlineDblink = onlineDblinkService.getById(dblinkId);
        if (onlineDblink == null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        if (!StrUtil.equals(onlineDblink.getAppCode(), TokenData.takeFromRequest().getAppCode())) {
            return ResponseResult.error(
                    ErrorCodeEnum.DATA_VALIDATED_FAILED, "数据验证失败，当前应用并不存在该数据库链接！");
        }
        return ResponseResult.success(onlineDblink);
    }

    private void maskOffPassword(OnlineDblink dblink) {
        String passwdKey = "password";
        JSONObject configJson = JSON.parseObject(dblink.getConfiguration());
        if (configJson.containsKey(passwdKey)) {
            String password = configJson.getString(passwdKey);
            if (StrUtil.isNotBlank(password)) {
                configJson.put(passwdKey, StrUtil.repeat('*', password.length()));
                dblink.setConfiguration(configJson.toJSONString());
            }
        }
    }
}
