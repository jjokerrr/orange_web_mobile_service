package com.bupt.webadmin.upms.dto;

import com.bupt.common.core.validator.UpdateGroup;

import lombok.Data;

import javax.validation.constraints.*;

/**
 * SysDeptDto对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
public class SysDeptDto {

    /**
     * 部门Id。
     */
    @NotNull(message = "数据验证失败，部门Id不能为空！", groups = {UpdateGroup.class})
    private Long deptId;

    /**
     * 部门名称。
     */
    @NotBlank(message = "数据验证失败，部门名称不能为空！")
    private String deptName;

    /**
     * 显示顺序。
     */
    @NotNull(message = "数据验证失败，显示顺序不能为空！")
    private Integer showOrder;
}
