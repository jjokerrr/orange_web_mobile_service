package com.bupt.common.online.dto;

import com.bupt.common.core.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 在线表单数据表所在数据库链接Dto对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
public class OnlineDblinkDto {

    /**
     * 主键Id。
     */
    @NotNull(message = "数据验证失败，主键Id不能为空！", groups = {UpdateGroup.class})
    private Long dblinkId;

    /**
     * 链接中文名称。
     */
    @NotBlank(message = "数据验证失败，链接中文名称不能为空！")
    private String dblinkName;

    /**
     * 链接描述。
     */
    private String dblinkDescription;

    /**
     * 配置信息。
     */
    @NotBlank(message = "数据验证失败，配置信息不能为空！")
    private String configuration;

    /**
     * 数据库链接类型。
     */
    @NotNull(message = "数据验证失败，数据库链接类型不能为空！")
    private Integer dblinkType;
}
