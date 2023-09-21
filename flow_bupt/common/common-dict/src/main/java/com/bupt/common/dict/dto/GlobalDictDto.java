package com.bupt.common.dict.dto;

import com.bupt.common.core.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 全局系统字典Dto。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
public class GlobalDictDto {

    /**
     * 主键Id。
     */
    @NotNull(message = "数据验证失败，主键Id不能为空！", groups = {UpdateGroup.class})
    private Long dictId;

    /**
     * 字典编码。
     */
    @NotBlank(message = "数据验证失败，字典编码不能为空！")
    private String dictCode;

    /**
     * 字典中文名称。
     */
    @NotBlank(message = "数据验证失败，字典中文名称不能为空！")
    private String dictName;
}
