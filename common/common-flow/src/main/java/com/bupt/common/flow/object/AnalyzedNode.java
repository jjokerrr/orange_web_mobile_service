package com.bupt.common.flow.object;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * BPMN 执行路径分析后的节点对象。通常表示为一个并行网关、排他网关和开始节点。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
public class AnalyzedNode {
    /**
     * 并行网关的开始节点Id，排他网关的Id，StartEvent节点的Id。
     */
    private String startId;
    /**
     * 并行网关的结束节点Id，其他场景为NULL。
     */
    private String endId;
    /**
     * 上级节点的StartId。
     */
    private String parentId;
    /**
     * 是否为启动任务。
     */
    private boolean startEvent = false;
    /**
     * 是否为并行网关。
     */
    private boolean parallelGateway = true;
    /**
     * 只有根节点包含该数据。
     */
    List<List<String>> allRoads;
    /**
     * 所有子节点的列表。
     */
    private List<AnalyzedNode> childList = new LinkedList<>();
    /**
     * 每个节点包含的用户任务的路径列表。
     */
    private List<List<UserTaskInfo>> userTaskRoads = new LinkedList<>();
    /**
     * 节点下所有用户任务的列表。
     */
    @JSONField(serialize = false, deserialize = false)
    private List<String> allUserTasks = new LinkedList<>();

    @Data
    @AllArgsConstructor
    public static class UserTaskInfo {
        /**
         * 任务定义的id。
         */
        private String taskId;
        /**
         * 是否为多实例任务。
         */
        private boolean isMultiInstance;
        /**
         * 所属AnalyzedNode对象的startId值。
         */
        private String nodeId;
        /**
         * 路径索引。
         */
        private int roadIndex;
    }
}

