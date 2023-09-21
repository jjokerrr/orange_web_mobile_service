package com.bupt.common.mobile.dto;

import com.bupt.common.core.validator.ConstDictRef;
import com.bupt.common.core.validator.UpdateGroup;
import com.bupt.common.mobile.model.constant.MobileEntryType;
import lombok.Data;

import javax.validation.constraints.*;

/**
 * 移动端入口Dto对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
public class MobileEntryDto {

    /**
     * 主键Id。
     */
    @NotNull(message = "数据验证失败，主键Id不能为空！", groups = {UpdateGroup.class})
    private Long entryId;

    /**
     * 父Id。
     */
    private Long parentId;

    /**
     * 显示名称。
     */
    @NotBlank(message = "数据验证失败，显示名称不能为空！")
    private String entryName;

    /**
     * 移动端入口类型。
     */
    @ConstDictRef(constDictClass = MobileEntryType.class, message = "数据验证失败，移动端入口类型值无效！")
    @NotNull(message = "数据验证失败，移动端入口类型不能为空！")
    private Integer entryType;

    /**
     * 是否对所有角色可见。
     */
    @NotNull(message = "数据验证失败，是否对所有角色可见标记不能为空！")
    private Boolean commonEntry;

    /**
     * 附件信息。
     */
    private String extraData;

    /**
     * 显示图片。
     */
    private String imageData;

    /**
     * 显示顺序。
     */
    @NotNull(message = "数据验证失败，显示顺序不能为空！")
    private Integer showOrder;
}
