package com.bupt.common.flow.controller;

import com.alibaba.fastjson.JSONObject;
import com.bupt.common.core.annotation.MyRequestBody;
import com.bupt.common.core.object.*;
import com.bupt.common.core.constant.ErrorCodeEnum;
import com.bupt.common.core.util.MyPageUtil;
import com.bupt.common.flow.model.constant.FlowMessageType;
import com.bupt.common.flow.model.FlowMessage;
import com.bupt.common.flow.service.FlowMessageService;
import com.bupt.common.flow.vo.FlowMessageVo;
import com.github.pagehelper.page.PageMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工作流消息接口。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Slf4j
@RestController
@RequestMapping("${common-flow.urlPrefix}/flowMessage")
@ConditionalOnProperty(name = "common-flow.operationEnabled", havingValue = "true")
public class FlowMessageController {

    @Autowired
    private FlowMessageService flowMessageService;

    /**
     * 获取当前用户的未读消息总数。
     * NOTE：白名单接口。
     *
     * @return 应答结果对象，包含当前用户的未读消息总数。
     */
    @GetMapping("/getMessageCount")
    public ResponseResult<JSONObject> getMessageCount() {
        JSONObject resultData = new JSONObject();
        resultData.put("remindingMessageCount", flowMessageService.countRemindingMessageListByUser());
        resultData.put("copyMessageCount", flowMessageService.countCopyMessageByUser());
        return ResponseResult.success(resultData);
    }

    /**
     * 获取当前用户的催办消息列表。
     * 不仅仅包含，其中包括当前用户所属角色、部门和岗位的候选组催办消息。
     * NOTE：白名单接口。
     *
     * @return 应答结果对象，包含查询结果集。
     */
    @PostMapping("/listRemindingTask")
    public ResponseResult<MyPageData<FlowMessageVo>> listRemindingTask(@MyRequestBody MyPageParam pageParam) {
        if (pageParam != null) {
            PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize(), pageParam.getCount());
        }
        List<FlowMessage> flowMessageList = flowMessageService.getRemindingMessageListByUser();
        return ResponseResult.success(MyPageUtil.makeResponseData(flowMessageList, FlowMessage.INSTANCE));
    }

    /**
     * 获取当前用户的抄送消息列表。
     * 不仅仅包含，其中包括当前用户所属角色、部门和岗位的候选组抄送消息。
     * NOTE：白名单接口。
     *
     * @param read true表示已读，false表示未读。
     * @return 应答结果对象，包含查询结果集。
     */
    @PostMapping("/listCopyMessage")
    public ResponseResult<MyPageData<FlowMessageVo>> listCopyMessage(
            @MyRequestBody MyPageParam pageParam, @MyRequestBody Boolean read) {
        if (pageParam != null) {
            PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize(), pageParam.getCount());
        }
        List<FlowMessage> flowMessageList = flowMessageService.getCopyMessageListByUser(read);
        return ResponseResult.success(MyPageUtil.makeResponseData(flowMessageList, FlowMessage.INSTANCE));
    }

    /**
     * 读取抄送消息，同时更新当前用户对指定抄送消息的读取状态。
     *
     * @param messageId 消息Id。
     * @return 应答结果对象。
     */
    @PostMapping("/readCopyTask")
    public ResponseResult<Void> readCopyTask(@MyRequestBody Long messageId) {
        String errorMessage;
        // 验证流程任务的合法性。
        FlowMessage flowMessage = flowMessageService.getById(messageId);
        if (flowMessage == null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        if (flowMessage.getMessageType() != FlowMessageType.COPY_TYPE) {
            errorMessage = "数据验证失败，当前消息不是抄送类型消息！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        if (!flowMessageService.isCandidateIdentityOnMessage(messageId)) {
            errorMessage = "数据验证失败，当前用户没有权限访问该消息！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        flowMessageService.readCopyTask(messageId);
        return ResponseResult.success();
    }
}
