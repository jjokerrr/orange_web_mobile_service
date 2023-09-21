package com.bupt.webadmin.upms.dao;

import com.bupt.common.core.base.dao.BaseDaoMapper;
import com.bupt.webadmin.upms.model.SysUser;
import org.apache.ibatis.annotations.Param;

import java.util.*;

/**
 * 用户管理数据操作访问接口。
 *
 * @author zzh
 * @date 2023-08-10
 */
public interface SysUserMapper extends BaseDaoMapper<SysUser> {

    /**
     * 批量插入对象列表。
     *
     * @param sysUserList 新增对象列表。
     */
    void insertList(List<SysUser> sysUserList);

    /**
     * 获取过滤后的对象列表。
     *
     * @param sysUserFilter 主表过滤对象。
     * @param orderBy 排序字符串，order by从句的参数。
     * @return 对象列表。
     */
    List<SysUser> getSysUserList(
            @Param("sysUserFilter") SysUser sysUserFilter, @Param("orderBy") String orderBy);

    /**
     * 根据部门Id集合，获取关联的用户列表。
     *
     * @param deptIds       关联的部门Id集合。
     * @param sysUserFilter 用户过滤条件对象。
     * @param orderBy       order by从句的参数。
     * @return 和部门Id集合关联的用户列表。
     */
    List<SysUser> getSysUserListByDeptIds(
            @Param("deptIds") Set<Long> deptIds,
            @Param("sysUserFilter") SysUser sysUserFilter,
            @Param("orderBy") String orderBy);

    /**
     * 根据登录名集合，获取关联的用户列表。
     * @param loginNames    登录名集合。
     * @param sysUserFilter 用户过滤条件对象。
     * @param orderBy       order by从句的参数。
     * @return 和登录名集合关联的用户列表。
     */
    List<SysUser> getSysUserListByLoginNames(
            @Param("loginNames") List<String> loginNames,
            @Param("sysUserFilter") SysUser sysUserFilter,
            @Param("orderBy") String orderBy);

    /**
     * 根据角色Id，获取关联的用户列表。
     *
     * @param roleId        关联的角色Id。
     * @param sysUserFilter 用户过滤条件对象。
     * @param orderBy       order by从句的参数。
     * @return 和角色Id关联的用户列表。
     */
    List<SysUser> getSysUserListByRoleId(
            @Param("roleId") Long roleId,
            @Param("sysUserFilter") SysUser sysUserFilter,
            @Param("orderBy") String orderBy);

    /**
     * 根据角色Id集合，获取去重后的用户Id列表。
     *
     * @param roleIds       关联的角色Id集合。
     * @param sysUserFilter 用户过滤条件对象。
     * @param orderBy       order by从句的参数。
     * @return 和角色Id集合关联的去重后的用户Id列表。
     */
    List<Long> getUserIdListByRoleIds(
            @Param("roleIds") Set<Long> roleIds,
            @Param("sysUserFilter") SysUser sysUserFilter,
            @Param("orderBy") String orderBy);

    /**
     * 根据角色Id，获取和当前角色Id没有建立多对多关联关系的用户列表。
     *
     * @param roleId        关联的角色Id。
     * @param sysUserFilter 用户过滤条件对象。
     * @param orderBy order by从句的参数。
     * @return 和RoleId没有建立关联关系的用户列表。
     */
    List<SysUser> getNotInSysUserListByRoleId(
            @Param("roleId") Long roleId,
            @Param("sysUserFilter") SysUser sysUserFilter,
            @Param("orderBy") String orderBy);

    /**
     * 查询用户的权限资源地址列表。同时返回详细的分配路径。
     *
     * @param userId 用户Id。
     * @param url    url过滤条件。
     * @return 包含从用户到权限资源的完整权限分配路径信息的查询结果列表。
     */
    List<Map<String, Object>> getSysPermListWithDetail(
            @Param("userId") Long userId, @Param("url") String url);

    /**
     * 查询用户的权限字列表。同时返回详细的分配路径。
     *
     * @param userId   用户Id。
     * @param permCode 权限字名称过滤条件。
     * @return 包含从用户到权限字的权限分配路径信息的查询结果列表。
     */
    List<Map<String, Object>> getSysPermCodeListWithDetail(
            @Param("userId") Long userId, @Param("permCode") String permCode);

    /**
     * 查询用户的菜单列表。同时返回详细的分配路径。
     *
     * @param userId   用户Id。
     * @param menuName 菜单名称过滤条件。
     * @return 包含从用户到菜单的权限分配路径信息的查询结果列表。
     */
    List<Map<String, Object>> getSysMenuListWithDetail(
            @Param("userId") Long userId, @Param("menuName") String menuName);
}
