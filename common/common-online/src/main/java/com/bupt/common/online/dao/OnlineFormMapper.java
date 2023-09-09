package com.bupt.common.online.dao;

import com.bupt.common.core.base.dao.BaseDaoMapper;
import com.bupt.common.online.model.OnlineForm;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 在线表单数据操作访问接口。
 *
 * @author zzh
 * @date 2023-08-10
 */
public interface OnlineFormMapper extends BaseDaoMapper<OnlineForm> {

    /**
     * 获取过滤后的对象列表。
     *
     * @param onlineFormFilter 主表过滤对象。
     * @param orderBy          排序字符串，order by从句的参数。
     * @return 对象列表。
     */
    List<OnlineForm> getOnlineFormList(
            @Param("onlineFormFilter") OnlineForm onlineFormFilter, @Param("orderBy") String orderBy);

    /**
     * 根据数据源Id，返回使用该数据源的OnlineForm对象。
     *
     * @param datasourceId     数据源Id。
     * @param onlineFormFilter 主表过滤对象。
     * @return 使用该数据源的表单列表。
     */
    List<OnlineForm> getOnlineFormListByDatasourceId(
            @Param("datasourceId") Long datasourceId, @Param("onlineFormFilter") OnlineForm onlineFormFilter);
}
