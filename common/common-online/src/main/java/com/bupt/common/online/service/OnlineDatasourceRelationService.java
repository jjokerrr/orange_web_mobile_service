package com.bupt.common.online.service;

import com.bupt.common.core.base.service.IBaseService;
import com.bupt.common.dbutil.object.SqlTable;
import com.bupt.common.dbutil.object.SqlTableColumn;
import com.bupt.common.online.model.OnlineDatasourceRelation;

import java.util.List;
import java.util.Set;

/**
 * 数据关联数据操作服务接口。
 *
 * @author zzh
 * @date 2023-08-10
 */
public interface OnlineDatasourceRelationService extends IBaseService<OnlineDatasourceRelation, Long> {

    /**
     * 保存新增对象。
     *
     * @param relation       新增对象。
     * @param slaveSqlTable  新增的关联从数据表对象。
     * @param slaveSqlColumn 新增的关联从数据表对象。
     * @return 返回新增对象。
     */
    OnlineDatasourceRelation saveNew(
            OnlineDatasourceRelation relation, SqlTable slaveSqlTable, SqlTableColumn slaveSqlColumn);

    /**
     * 更新数据对象。
     *
     * @param relation         更新的对象。
     * @param originalRelation 原有数据对象。
     * @return 成功返回true，否则false。
     */
    boolean update(OnlineDatasourceRelation relation, OnlineDatasourceRelation originalRelation);

    /**
     * 删除指定数据。
     *
     * @param relationId 主键Id。
     * @return 成功返回true，否则false。
     */
    boolean remove(Long relationId);

    /**
     * 当前服务的支持表为从表，根据主表的主键Id，删除一对多的从表数据。
     *
     * @param datasourceId 主表主键Id。
     * @return 删除数量。
     */
    int removeByDatasourceId(Long datasourceId);

    /**
     * 查询指定数据源Id的数据源关联对象列表。
     * 从缓存中读取，如果不存在会从数据库中读取并同步到Redis中。
     *
     * @param datasourceIdSet 数据源Id集合。
     * @return 在线数据源关联对象列表。
     */
    List<OnlineDatasourceRelation> getOnlineDatasourceRelationListFromCache(Set<Long> datasourceIdSet);

    /**
     * 查询指定数据源关联对象。
     * 从缓存中读取，如果不存在会从数据库中读取并同步到Redis中。
     *
     * @param datasourceId 数据源Id。
     * @param relationId   数据源关联Id。
     * @return 在线数据源关联对象。
     */
    OnlineDatasourceRelation getOnlineDatasourceRelationFromCache(Long datasourceId, Long relationId);

    /**
     * 获取主表的查询结果，以及主表关联的字典数据和一对一从表数据，以及一对一从表的字典数据。
     * 该查询会涉及到一对一从表的关联过滤，或一对多从表的嵌套关联过滤，因此性能不如单表过滤。
     * 如果仅仅需要获取主表数据，请移步(getOnlineDatasourceRelationList)，以便获取更好的查询性能。
     *
     * @param filter  主表过滤对象。
     * @param orderBy 排序参数。
     * @return 查询结果集。
     */
    List<OnlineDatasourceRelation> getOnlineDatasourceRelationListWithRelation(
            OnlineDatasourceRelation filter, String orderBy);
}
