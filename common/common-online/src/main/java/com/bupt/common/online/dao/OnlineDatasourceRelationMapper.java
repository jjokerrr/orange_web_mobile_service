package com.bupt.common.online.dao;

import com.bupt.common.core.base.dao.BaseDaoMapper;
import com.bupt.common.online.model.OnlineDatasourceRelation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据关联数据操作访问接口。
 *
 * @author zzh
 * @date 2023-08-10
 */
public interface OnlineDatasourceRelationMapper extends BaseDaoMapper<OnlineDatasourceRelation> {

    /**
     * 获取过滤后的对象列表。
     *
     * @param filter  主表过滤对象。
     * @param orderBy 排序字符串，order by从句的参数。
     * @return 对象列表。
     */
    List<OnlineDatasourceRelation> getOnlineDatasourceRelationList(
            @Param("filter") OnlineDatasourceRelation filter, @Param("orderBy") String orderBy);
}
