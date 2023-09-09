package com.bupt.common.online.dao;

import com.bupt.common.core.base.dao.BaseDaoMapper;
import com.bupt.common.online.model.OnlineDatasource;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 数据模型数据操作访问接口。
 *
 * @author zzh
 * @date 2023-08-10
 */
public interface OnlineDatasourceMapper extends BaseDaoMapper<OnlineDatasource> {

    /**
     * 获取过滤后的对象列表。
     *
     * @param onlineDatasourceFilter 主表过滤对象。
     * @param orderBy                排序字符串，order by从句的参数。
     * @return 对象列表。
     */
    List<OnlineDatasource> getOnlineDatasourceList(
            @Param("onlineDatasourceFilter") OnlineDatasource onlineDatasourceFilter, @Param("orderBy") String orderBy);

    /**
     * 根据关联主表Id，获取关联从表数据列表。
     *
     * @param pageId                 关联主表Id。
     * @param onlineDatasourceFilter 从表过滤对象。
     * @param orderBy                排序字符串，order by从句的参数。
     * @return 从表数据列表。
     */
    List<OnlineDatasource> getOnlineDatasourceListByPageId(
            @Param("pageId") Long pageId,
            @Param("onlineDatasourceFilter") OnlineDatasource onlineDatasourceFilter,
            @Param("orderBy") String orderBy);

    /**
     * 根据在线表单Id集合，获取关联的在线数据源对象列表。
     *
     * @param formIdSet 在线表单Id集合。
     * @return 与参数表单Id关联的数据源列表。
     */
    List<OnlineDatasource> getOnlineDatasourceListByFormIds(@Param("formIdSet") Set<Long> formIdSet);

    /**
     * 获取在线表单页面和在线表单数据源变量名的映射关系。
     *
     * @param pageIds 页面Id集合。
     * @return 在线表单页面和在线表单数据源变量名的映射关系。
     */
    @Select("SELECT a.page_id, b.variable_name FROM zz_online_page_datasource a, zz_online_datasource b" +
            " WHERE a.page_id in (${pageIds}) AND a.datasource_id = b.datasource_id")
    List<Map<String, Object>> getPageIdAndVariableNameMapByPageIds(@Param("pageIds") String pageIds);
}
