package com.bupt.common.flow.dto;

import com.bupt.common.core.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 流程分类的Dto对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
public class FlowCategoryDto {

    /**
     * 主键Id。
     */
    @NotNull(message = "数据验证失败，主键Id不能为空！", groups = {UpdateGroup.class})
    private Long categoryId;

    /**
     * 显示名称。
     */
    @NotBlank(message = "数据验证失败，显示名称不能为空！")
    private String name;

    /**
     * 分类编码。
     */
    @NotBlank(message = "数据验证失败，分类编码不能为空！")
    private String code;

    /**
     * 实现顺序。
     */
    @NotNull(message = "数据验证失败，实现顺序不能为空！")
    private Integer showOrder;
}
