package com.bupt.common.flow.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bupt.common.core.base.mapper.BaseModelMapper;
import com.bupt.common.flow.vo.FlowVariableDisplayVo;
import lombok.Data;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


@Data
@TableName(value = "zz_flow_variable_display")
public class FlowVariableDisplay {

    /**
     * 变量Id。
     */
    @TableId(value = "variable_id")
    private String variableId;

    /**
     * 任务key。
     */
    @TableField(value = "task_key")
    private String taskKey;

    /**
     * 变量标签。
     */
    @TableField(value = "variable_label")
    private String variableLabel;

    /**
     * 变量显示方式。
     */
    @TableField(value = "variable_authority")
    private String variableAuthority;

    @TableField(value = "entry_id")
    private String entryId;


    @Mapper
    public interface FlowVariableDisplayModelMapper extends BaseModelMapper<FlowVariableDisplayVo, FlowVariableDisplay> {
    }
    public static final FlowVariableDisplayModelMapper INSTANCE = Mappers.getMapper(FlowVariableDisplayModelMapper.class);
}
