package com.bupt.common.online.dto;

import com.bupt.common.core.validator.ConstDictRef;
import com.bupt.common.core.validator.UpdateGroup;
import com.bupt.common.online.model.constant.PageStatus;
import com.bupt.common.online.model.constant.PageType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 在线表单所在页面Dto对象。这里我们可以把页面理解为表单的容器。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
public class OnlinePageDto {

    /**
     * 主键Id。
     */
    @NotNull(message = "数据验证失败，主键Id不能为空！", groups = {UpdateGroup.class})
    private Long pageId;

    /**
     * 页面编码。
     */
    private String pageCode;

    /**
     * 页面名称。
     */
    @NotBlank(message = "数据验证失败，页面名称不能为空！")
    private String pageName;

    /**
     * 页面类型。
     */
    @NotNull(message = "数据验证失败，页面类型不能为空！")
    @ConstDictRef(constDictClass = PageType.class, message = "数据验证失败，页面类型为无效值！")
    private Integer pageType;

    /**
     * 页面编辑状态。
     */
    @NotNull(message = "数据验证失败，状态不能为空！")
    @ConstDictRef(constDictClass = PageStatus.class, message = "数据验证失败，状态为无效值！")
    private Integer status;
}
