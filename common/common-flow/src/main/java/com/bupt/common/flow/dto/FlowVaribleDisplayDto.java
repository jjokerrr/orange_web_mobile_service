package com.bupt.common.flow.dto;

import com.bupt.common.core.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class FlowVaribleDisplayDto {
    /**
     * 主键 variableId。
     */
    @NotNull(message = "数据验证失败，主键variableId不能为空！", groups = {UpdateGroup.class})
    private String variableId;

    /**
     * 主键 taskKey。
     */
    @NotNull(message = "数据验证失败，主键taskKey不能为空！", groups = {UpdateGroup.class})
    private String taskKey;

    /**
     * 显示名称 variableLabel。
     */
    @NotBlank(message = "数据验证失败，显示名称不能为空！")
    private String variableLabel;

    /**
     * 显示权限 variableAuthority。
     */
    @NotBlank(message = "数据验证失败，显示权限不能为空！")
    private String variableAuthority;

    @NotBlank(message = "数据验证失败，显示权限不能为空！")
    private String entryId;
}
