package com.bupt.common.flow.service;

import com.alibaba.fastjson.JSONObject;
import com.bupt.common.core.base.service.IBaseService;
import com.bupt.common.flow.model.FlowMessage;
import com.bupt.common.flow.model.FlowWorkOrder;
import org.flowable.task.api.Task;

import java.util.List;

/**
 * 工作流消息数据操作服务接口。
 *
 * @author zzh
 * @date 2023-08-10
 */
public interface FlowMessageService extends IBaseService<FlowMessage, Long> {

    /**
     * 保存新增对象。
     *
     * @param flowMessage 新增对象。
     * @return 保存后的消息对象。
     */
    FlowMessage saveNew(FlowMessage flowMessage);

    /**
     * 根据工单参数，保存催单消息对象。如果当前工单存在多个待办任务，则插入多条催办消息数据。
     *
     * @param flowWorkOrder 待催办的工单。
     */
    void saveNewRemindMessage(FlowWorkOrder flowWorkOrder);

    /**
     * 保存抄送消息对象。
     *
     * @param task          待抄送的任务。
     * @param copyDataJson  抄送人员或者组的Id数据。
     */
    void saveNewCopyMessage(Task task, JSONObject copyDataJson);

    /**
     * 更新指定运行时任务Id的消费为已完成状态。
     *
     * @param taskId 运行时任务Id。
     */
    void updateFinishedStatusByTaskId(String taskId);

    /**
     * 更新指定流程实例Id的消费为已完成状态。
     *
     * @param processInstanceId 流程实例IdId。
     */
    void updateFinishedStatusByProcessInstanceId(String processInstanceId);

    /**
     * 获取当前用户的催办消息列表。
     *
     * @return 查询后的催办消息列表。
     */
    List<FlowMessage> getRemindingMessageListByUser();

    /**
     * 获取当前用户的抄送消息列表。
     *
     * @param read true表示已读，false表示未读。
     * @return 查询后的抄送消息列表。
     */
    List<FlowMessage> getCopyMessageListByUser(Boolean read);

    /**
     * 判断当前用户是否有权限访问指定消息Id。
     *
     * @param messageId 消息Id。
     * @return true为合法访问者，否则false。
     */
    boolean isCandidateIdentityOnMessage(Long messageId);

    /**
     * 读取抄送消息，同时更新当前用户对指定抄送消息的读取状态。
     *
     * @param messageId 消息Id。
     */
    void readCopyTask(Long messageId);

    /**
     * 计算当前用户催办消息的数量。
     *
     * @return 当前用户催办消息数量。
     */
    int countRemindingMessageListByUser();

    /**
     * 计算当前用户未读抄送消息的数量。
     *
     * @return 当前用户未读抄送消息数量。
     */
    int countCopyMessageByUser();

    /**
     * 删除指定流程实例的消息。
     *
     * @param processInstanceId 流程实例Id。
     */
    void removeByProcessInstanceId(String processInstanceId);
}
