package com.bupt.common.flow.dao;

import com.bupt.common.core.annotation.EnableDataPerm;
import com.bupt.common.core.base.dao.BaseDaoMapper;
import com.bupt.common.flow.model.FlowWorkOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.*;

/**
 * 工作流工单表数据操作访问接口。
 * 如果当前系统支持数据权限过滤，当前用户必须要能看自己的工单数据，所以需要把EnableDataPerm
 * 的mustIncludeUserRule参数设置为true，即便当前用户的数据权限中并不包含DataPermRuleType.TYPE_USER_ONLY，
 * 数据过滤拦截组件也会自动补偿该类型的数据权限，以便当前用户可以看到自己发起的工单。
 *
 * @author zzh
 * @date 2023-08-10
 */
@EnableDataPerm(mustIncludeUserRule = true)
public interface FlowWorkOrderMapper extends BaseDaoMapper<FlowWorkOrder> {

    /**
     * 获取过滤后的对象列表。
     *
     * @param flowWorkOrderFilter 主表过滤对象。
     * @param orderBy 排序字符串，order by从句的参数。
     * @return 对象列表。
     */
    List<FlowWorkOrder> getFlowWorkOrderList(
            @Param("flowWorkOrderFilter") FlowWorkOrder flowWorkOrderFilter, @Param("orderBy") String orderBy);

    /**
     * 计算指定前缀工单编码的最大值。
     *
     * @param prefix 工单编码前缀。
     * @return 该工单编码前缀的最大值。
     */
    @Select("SELECT MAX(work_order_code) FROM zz_flow_work_order WHERE work_order_code LIKE '${prefix}'")
    String getMaxWorkOrderCodeByPrefix(@Param("prefix") String prefix);

    /**
     * 根据工单编码查询指定工单，查询过程也会考虑逻辑删除的数据。
     * @param workOrderCode 工单编码。
     * @return
     */
    @Select("SELECT COUNT(*) FROM zz_flow_work_order WHERE work_order_code = #{workOrderCode}")
    int getCountByWorkOrderCode(@Param("workOrderCode") String workOrderCode);
}
