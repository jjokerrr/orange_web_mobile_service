package com.bupt.common.online.service;

import com.bupt.common.core.base.service.IBaseService;
import com.bupt.common.dbutil.object.SqlTable;
import com.bupt.common.online.model.OnlineColumn;
import com.bupt.common.online.model.OnlineTable;

import java.util.List;
import java.util.Set;

/**
 * 数据表数据操作服务接口。
 *
 * @author zzh
 * @date 2023-08-10
 */
public interface OnlineTableService extends IBaseService<OnlineTable, Long> {

    /**
     * 基于数据库表保存新增对象。
     *
     * @param sqlTable 数据库表对象。
     * @return 返回新增对象。
     */
    OnlineTable saveNewFromSqlTable(SqlTable sqlTable);

    /**
     * 删除指定表及其关联的字段数据。
     *
     * @param tableId 主键Id。
     * @return 成功返回true，否则false。
     */
    boolean remove(Long tableId);

    /**
     * 删除指定数据表Id集合中的表，及其关联字段。
     *
     * @param tableIdSet 待删除的数据表Id集合。
     */
    void removeByTableIdSet(Set<Long> tableIdSet);

    /**
     * 根据数据源Id，获取该数据源及其关联所引用的数据表列表。
     *
     * @param datasourceId 指定的数据源Id。
     * @return 该数据源及其关联所引用的数据表列表。
     */
    List<OnlineTable> getOnlineTableListByDatasourceId(Long datasourceId);

    /**
     * 从缓存中获取指定的表数据及其关联字段列表。优先从缓存中读取，如果不存在则从数据库中读取，并同步到缓存。
     * 该接口方法仅仅用户在线表单的动态数据操作接口，而非在线表单的配置接口。
     *
     * @param tableId 表主键Id。
     * @return 查询后的在线表对象。
     */
    OnlineTable getOnlineTableFromCache(Long tableId);

    /**
     * 从缓存中获取指定的表字段。优先从缓存中读取，如果不存在则从数据库中读取，并同步到缓存。
     * 该接口方法仅仅用户在线表单的动态数据操作接口，而非在线表单的配置接口。
     *
     * @param tableId  表主键Id。
     * @param columnId 字段Id。
     * @return 查询后的在线表对象。
     */
    OnlineColumn getOnlineColumnFromCache(Long tableId, Long columnId);
}
