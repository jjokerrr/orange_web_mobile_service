package com.bupt.common.mobile.service;

import com.bupt.common.core.base.service.IBaseService;
import com.bupt.common.mobile.model.MobileEntry;

import java.util.*;

/**
 * 移动端首页入口管理数据操作服务接口。
 *
 * @author zzh
 * @date 2023-08-10
 */
public interface MobileEntryService extends IBaseService<MobileEntry, Long> {

    /**
     * 保存新增对象。
     *
     * @param mobileEntry      新增对象。
     * @param roleIdListString 逗号分隔的角色Id列表。
     * @return 返回新增对象。
     */
    MobileEntry saveNew(MobileEntry mobileEntry, String roleIdListString);

    /**
     * 更新数据对象。
     *
     * @param mobileEntry         更新的对象。
     * @param originalMobileEntry 原有数据对象。
     * @param roleIdListString    逗号分隔的角色Id列表。
     * @return 成功返回true，否则false。
     */
    boolean update(MobileEntry mobileEntry, MobileEntry originalMobileEntry, String roleIdListString);

    /**
     * 删除指定数据。
     *
     * @param entryId 主键Id。
     * @return 成功返回true，否则false。
     */
    boolean remove(Long entryId);

    /**
     * 获取单表查询结果。由于没有关联数据查询，因此在仅仅获取单表数据的场景下，效率更高。
     * 如果需要同时获取关联数据，请移步(getMobileEntryListWithRelation)方法。
     *
     * @param filter  过滤对象。
     * @param orderBy 排序参数。
     * @return 查询结果集。
     */
    List<MobileEntry> getMobileEntryList(MobileEntry filter, String orderBy);

    /**
     * 获取主表的查询结果，以及主表关联的字典数据和一对一从表数据，以及一对一从表的字典数据。
     * 该查询会涉及到一对一从表的关联过滤，或一对多从表的嵌套关联过滤，因此性能不如单表过滤。
     * 如果仅仅需要获取主表数据，请移步(getMobileEntryList)，以便获取更好的查询性能。
     *
     * @param filter  主表过滤对象。
     * @param orderBy 排序参数。
     * @return 查询结果集。
     */
    List<MobileEntry> getMobileEntryListWithRelation(MobileEntry filter, String orderBy);

    /**
     * 获取属于指定角色Id集合的移动端入口列表。
     *
     * @param roleIds 逗号分隔的角色Id集合。
     * @return 移动端入口对象列表。
     */
    List<MobileEntry> getMobileEntryListByRoleIds(String roleIds);
}
