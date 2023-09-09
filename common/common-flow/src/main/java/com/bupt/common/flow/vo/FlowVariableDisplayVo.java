package com.bupt.common.flow.vo;

import lombok.Data;

@Data
public class FlowVariableDisplayVo {

    /**
     * 变量Id。
     */
    private String variableId;

    /**
     * 任务key。
     */
    private String taskKey;

    /**
     * 变量标签。
     */
    private String variableLabel;

    /**
     * 变量显示方式。
     */
    private String variableAuthority;

    private String entryId;
}
