package com.bupt.common.flow.controller;

import com.bupt.common.core.annotation.MyRequestBody;
import com.bupt.common.core.constant.ErrorCodeEnum;
import com.bupt.common.core.object.MyPageData;
import com.bupt.common.core.object.ResponseResult;
import com.bupt.common.core.util.MyPageUtil;
import com.bupt.common.flow.model.FlowVariableDisplay;
import com.bupt.common.flow.service.FlowVariableDisplayService;
import com.bupt.common.flow.vo.FlowVariableDisplayVo;
import com.bupt.common.log.annotation.OperationLog;
import com.bupt.common.log.model.constant.SysOperationLogType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("${common-flow.urlPrefix}/flowVariableDisplay")
@ConditionalOnProperty(name = "common-flow.operationEnabled", havingValue = "true")
public class FlowVariableDisplayController {

    @Autowired
    private FlowVariableDisplayService flowVariableDisplayService;

    @OperationLog(type = SysOperationLogType.DELETE)
    @PostMapping("/deleteByTask")
    public ResponseResult<Void> deleteByTask(@MyRequestBody String taskKey) {
        String errorMessage;
        if (!flowVariableDisplayService.removeByTaskKey(taskKey)) {
            errorMessage = "数据操作失败，删除的对象不存在，请刷新后重试！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        return ResponseResult.success();
    }

    @OperationLog(type = SysOperationLogType.DELETE)
    @PostMapping("/deleteByEntry")
    public ResponseResult<Void> deleteByEntry(@MyRequestBody String entryId) {
        String errorMessage;
        if (!flowVariableDisplayService.removeByEntry(entryId)) {
            errorMessage = "数据操作失败，删除的对象不存在，请刷新后重试！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        return ResponseResult.success();
    }

    @PostMapping("/list")
    public ResponseResult<MyPageData<FlowVariableDisplayVo>> list(@MyRequestBody String taskKey) {
        List<FlowVariableDisplay> flowVariableSisplayList =
                flowVariableDisplayService.select(taskKey);
        return ResponseResult.success(MyPageUtil.makeResponseData(flowVariableSisplayList, FlowVariableDisplay.INSTANCE));
    }

    @PostMapping("/listByEntry")
    public ResponseResult<MyPageData<FlowVariableDisplayVo>> listByEntry(@MyRequestBody String entryId) {
        List<FlowVariableDisplay> flowVariableSisplayList =
                flowVariableDisplayService.selectByEntry(entryId);
        return ResponseResult.success(MyPageUtil.makeResponseData(flowVariableSisplayList, FlowVariableDisplay.INSTANCE));
    }
}
