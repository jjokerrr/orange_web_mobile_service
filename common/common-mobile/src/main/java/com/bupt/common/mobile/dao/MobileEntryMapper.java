package com.bupt.common.mobile.dao;

import com.bupt.common.core.base.dao.BaseDaoMapper;
import com.bupt.common.mobile.model.MobileEntry;
import org.apache.ibatis.annotations.Param;

import java.util.*;

/**
 * 移动端首页入口管理数据操作访问接口。
 *
 * @author zzh
 * @date 2023-08-10
 */
public interface MobileEntryMapper extends BaseDaoMapper<MobileEntry> {

    /**
     * 获取过滤后的对象列表。
     *
     * @param mobileEntryFilter 主表过滤对象。
     * @param orderBy 排序字符串，order by从句的参数。
     * @return 对象列表。
     */
    List<MobileEntry> getMobileEntryList(
            @Param("mobileEntryFilter") MobileEntry mobileEntryFilter, @Param("orderBy") String orderBy);

    /**
     * 获取属于指定角色Id集合的移动端入口列表。
     *
     * @param roleIds 角色Id集合。
     * @return 移动端入口对象列表。
     */
    List<MobileEntry> getMobileEntryListByRoleIds(@Param("roleIds") Set<Long> roleIds);
}
