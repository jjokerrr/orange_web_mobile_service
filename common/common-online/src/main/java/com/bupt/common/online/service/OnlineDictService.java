package com.bupt.common.online.service;

import com.bupt.common.core.base.service.IBaseService;
import com.bupt.common.online.model.OnlineDict;

import java.util.List;
import java.util.Set;

/**
 * 在线表单字典数据操作服务接口。
 *
 * @author zzh
 * @date 2023-08-10
 */
public interface OnlineDictService extends IBaseService<OnlineDict, Long> {

    /**
     * 保存新增对象。
     *
     * @param onlineDict 新增对象。
     * @return 返回新增对象。
     */
    OnlineDict saveNew(OnlineDict onlineDict);

    /**
     * 更新数据对象。
     *
     * @param onlineDict         更新的对象。
     * @param originalOnlineDict 原有数据对象。
     * @return 成功返回true，否则false。
     */
    boolean update(OnlineDict onlineDict, OnlineDict originalOnlineDict);

    /**
     * 删除指定数据。
     *
     * @param dictId 主键Id。
     * @return 成功返回true，否则false。
     */
    boolean remove(Long dictId);

    /**
     * 获取单表查询结果。由于没有关联数据查询，因此在仅仅获取单表数据的场景下，效率更高。
     * 如果需要同时获取关联数据，请移步(getOnlineDictListWithRelation)方法。
     *
     * @param filter  过滤对象。
     * @param orderBy 排序参数。
     * @return 查询结果集。
     */
    List<OnlineDict> getOnlineDictList(OnlineDict filter, String orderBy);

    /**
     * 获取主表的查询结果，以及主表关联的字典数据和一对一从表数据，以及一对一从表的字典数据。
     * 该查询会涉及到一对一从表的关联过滤，或一对多从表的嵌套关联过滤，因此性能不如单表过滤。
     * 如果仅仅需要获取主表数据，请移步(getOnlineDictList)，以便获取更好的查询性能。
     *
     * @param filter  主表过滤对象。
     * @param orderBy 排序参数。
     * @return 查询结果集。
     */
    List<OnlineDict> getOnlineDictListWithRelation(OnlineDict filter, String orderBy);

    /**
     * 从缓存中获取字典数据。
     *
     * @param dictId 字典Id。
     * @return 在线字典对象。
     */
    OnlineDict getOnlineDictFromCache(Long dictId);

    /**
     * 从缓存中获取字典数据集合。
     *
     * @param dictIdSet 字典Id集合。
     * @return 在线字典对象集合。
     */
    List<OnlineDict> getOnlineDictListFromCache(Set<Long> dictIdSet);
}
