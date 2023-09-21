package com.bupt.common.dict.dto;

import com.bupt.common.core.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 全局系统字典项目Dto。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
public class GlobalDictItemDto {

    /**
     * 主键Id。
     */
    @NotNull(message = "数据验证失败，主键Id不能为空！", groups = {UpdateGroup.class})
    private Long id;

    /**
     * 字典编码。
     */
    @NotBlank(message = "数据验证失败，字典编码不能为空！")
    private String dictCode;

    /**
     * 字典数据项Id。
     */
    @NotNull(message = "数据验证失败，字典数据项Id不能为空！")
    private String itemId;

    /**
     * 字典数据项名称。
     */
    @NotBlank(message = "数据验证失败，字典数据项名称不能为空！")
    private String itemName;

    /**
     * 显示顺序(数值越小越靠前)。
     */
    @NotNull(message = "数据验证失败，显示顺序不能为空！")
    private Integer showOrder;
}
