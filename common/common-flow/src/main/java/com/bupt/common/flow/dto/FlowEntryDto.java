package com.bupt.common.flow.dto;

import com.bupt.common.core.validator.ConstDictRef;
import com.bupt.common.core.validator.UpdateGroup;
import com.bupt.common.flow.model.constant.FlowBindFormType;
import com.bupt.common.flow.model.constant.FlowEntryStatus;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 流程的Dto对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
public class FlowEntryDto {

    /**
     * 主键Id。
     */
    @NotNull(message = "数据验证失败，主键不能为空！", groups = {UpdateGroup.class})
    private Long entryId;

    /**
     * 流程名称。
     */
    @NotBlank(message = "数据验证失败，流程名称不能为空！")
    private String processDefinitionName;

    /**
     * 流程标识Key。
     */
    @NotBlank(message = "数据验证失败，流程标识Key不能为空！")
    private String processDefinitionKey;

    /**
     * 流程分类。
     */
    @NotNull(message = "数据验证失败，流程分类不能为空！")
    private Long categoryId;

    /**
     * 流程状态。
     */
    @ConstDictRef(constDictClass = FlowEntryStatus.class, message = "数据验证失败，工作流状态为无效值！")
    private Integer status;

    /**
     * 流程定义的xml。
     */
    private String bpmnXml;

    /**
     * 流程图类型。0: 普通流程图，1: 钉钉风格的流程图。
     */
    private Integer diagramType;

    /**
     * 绑定表单类型。
     */
    @ConstDictRef(constDictClass = FlowBindFormType.class, message = "数据验证失败，工作流绑定表单类型为无效值！")
    @NotNull(message = "数据验证失败，工作流绑定表单类型不能为空！")
    private Integer bindFormType;

    /**
     * 在线表单的页面Id。
     */
    private Long pageId;

    /**
     * 在线表单的缺省路由名称。
     */
    private String defaultRouterName;

    /**
     * 在线表单Id。
     */
    private Long defaultFormId;

    /**
     * 工单表编码字段的编码规则，如果为空则不计算工单编码。
     */
    private String encodedRule;

    /**
     * 流程的自定义扩展数据(JSON格式)。
     */
    private String extensionData;

    /**
     * 流程的自定义数据操作权限。
     */
    private List<FlowVaribleDisplayDto> variableDisplay;

    private Boolean ifVariableDisplay;
}
