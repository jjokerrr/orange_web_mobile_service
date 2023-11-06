package com.bupt.webadmin.upms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.*;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.page.PageMethod;
import com.bupt.webadmin.upms.service.*;
import com.bupt.webadmin.upms.dao.*;
import com.bupt.webadmin.upms.model.*;
import com.bupt.webadmin.upms.model.constant.SysUserStatus;
import com.bupt.common.ext.util.BizWidgetDatasourceExtHelper;
import com.bupt.common.ext.base.BizWidgetDatasource;
import com.bupt.common.ext.constant.BizWidgetDatasourceType;
import com.bupt.common.core.base.dao.BaseDaoMapper;
import com.bupt.common.core.constant.UserFilterGroup;
import com.bupt.common.core.constant.GlobalDeletedFlag;
import com.bupt.common.core.object.*;
import com.bupt.common.core.base.service.BaseService;
import com.bupt.common.core.util.MyModelUtil;
import com.bupt.common.core.util.MyPageUtil;
import com.bupt.common.sequence.wrapper.IdGeneratorWrapper;
import com.github.pagehelper.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户管理数据操作服务类。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Slf4j
@Service("sysUserService")
public class SysUserServiceImpl extends BaseService<SysUser, Long> implements SysUserService, BizWidgetDatasource {

    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;
    @Autowired
    private SysDeptService sysDeptService;
    @Autowired
    private SysRoleService sysRoleService;
    @Autowired
    private IdGeneratorWrapper idGenerator;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private BizWidgetDatasourceExtHelper bizWidgetDatasourceExtHelper;

    /**
     * 返回当前Service的主表Mapper对象。
     *
     * @return 主表Mapper对象。
     */
    @Override
    protected BaseDaoMapper<SysUser> mapper() {
        return sysUserMapper;
    }

    @PostConstruct
    private void registerBizWidgetDatasource() {
        bizWidgetDatasourceExtHelper.registerDatasource(BizWidgetDatasourceType.UPMS_USER_TYPE, this);
    }

    @Override
    public MyPageData<Map<String, Object>> getDataList(
            String type, Map<String, Object> filter, MyOrderParam orderParam, MyPageParam pageParam) {
        if (pageParam != null) {
            PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize(), pageParam.getCount());
        }
        List<SysUser> userList = null;
        String orderBy = MyOrderParam.buildOrderBy(orderParam, SysUser.class, false);
        SysUser userFilter = BeanUtil.toBean(filter, SysUser.class);
        if (filter != null) {
            Object group = filter.get("USER_FILTER_GROUP");
            if (group != null) {
                JSONObject filterGroupJson = JSON.parseObject(group.toString());
                String groupType = filterGroupJson.getString("type");
                String values = filterGroupJson.getString("values");
                if (UserFilterGroup.USER.equals(groupType)) {
                    List<String> loginNames = StrUtil.splitTrim(values, ",");
                    userList = sysUserMapper.getSysUserListByLoginNames(loginNames, userFilter, orderBy);
                } else {
                    Set<Long> groupIds = StrUtil.splitTrim(values, ",")
                            .stream().map(Long::valueOf).collect(Collectors.toSet());
                    userList = this.getUserListByGroupIds(groupType, groupIds, userFilter, orderBy);
                }
            }
        }
        if (userList == null) {
            userList = this.getSysUserList(userFilter, orderBy);
        }
        this.buildRelationForDataList(userList, MyRelationParam.dictOnly());
        return MyPageUtil.makeResponseData(userList, BeanUtil::beanToMap);
    }

    private List<SysUser> getUserListByGroupIds(String groupType, Set<Long> groupIds, SysUser filter, String orderBy) {
        if (groupType.equals(UserFilterGroup.DEPT)) {
            return sysUserMapper.getSysUserListByDeptIds(groupIds, filter, orderBy);
        }
        List<Long> userIds = null;
        switch (groupType) {
            case UserFilterGroup.ROLE:
                userIds = sysUserMapper.getUserIdListByRoleIds(groupIds, filter, orderBy);
                break;
            default:
                break;
        }
        return CollUtil.isEmpty(userIds) ? null : sysUserMapper.selectBatchIds(userIds);
    }

    @Override
    public List<Map<String, Object>> getDataListWithInList(String type, String fieldName, List<String> fieldValues) {
        List<SysUser> userList;
        if (StrUtil.isBlank(fieldName)) {
            userList = this.getInList(fieldValues.stream().map(Long::valueOf).collect(Collectors.toSet()));
        } else {
            userList = this.getInList(fieldName, MyModelUtil.convertToTypeValues(SysUser.class, fieldName, fieldValues));
        }
        this.buildRelationForDataList(userList, MyRelationParam.dictOnly());
        return MyModelUtil.beanToMapList(userList);
    }

    /**
     * 获取指定登录名的用户对象。
     *
     * @param loginName 指定登录用户名。
     * @return 用户对象。
     */
    @Override
    public SysUser getSysUserByLoginName(String loginName) {
        SysUser filter = new SysUser();
        filter.setLoginName(loginName);
        return sysUserMapper.selectOne(new QueryWrapper<>(filter));
    }

    /**
     * 保存新增的用户对象。
     *
     * @param user      新增的用户对象。
     * @param roleIdSet 用户角色Id集合。
     * @return 新增后的用户对象。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public SysUser saveNew(SysUser user, Set<Long> roleIdSet) {
        user.setUserId(idGenerator.nextLongId());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setUserStatus(SysUserStatus.STATUS_NORMAL);
        user.setDeletedFlag(GlobalDeletedFlag.NORMAL);
        MyModelUtil.fillCommonsForInsert(user);
        sysUserMapper.insert(user);
        if (CollUtil.isNotEmpty(roleIdSet)) {
            for (Long roleId : roleIdSet) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(user.getUserId());
                userRole.setRoleId(roleId);
                sysUserRoleMapper.insert(userRole);
            }
        }
        return user;
    }

    /**
     * 更新用户对象。
     *
     * @param user          更新的用户对象。
     * @param originalUser  原有的用户对象。
     * @param roleIdSet     用户角色Id列表。
     * @return 更新成功返回true，否则false。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean update(SysUser user, SysUser originalUser, Set<Long> roleIdSet) {
        user.setLoginName(originalUser.getLoginName());
        user.setPassword(originalUser.getPassword());
        MyModelUtil.fillCommonsForUpdate(user, originalUser);
        UpdateWrapper<SysUser> uw = this.createUpdateQueryForNullValue(user, user.getUserId());
        if (sysUserMapper.update(user, uw) != 1) {
            return false;
        }
        // 先删除原有的User-Role关联关系，再重新插入新的关联关系
        SysUserRole deletedUserRole = new SysUserRole();
        deletedUserRole.setUserId(user.getUserId());
        sysUserRoleMapper.delete(new QueryWrapper<>(deletedUserRole));
        if (CollUtil.isNotEmpty(roleIdSet)) {
            for (Long roleId : roleIdSet) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(user.getUserId());
                userRole.setRoleId(roleId);
                sysUserRoleMapper.insert(userRole);
            }
        }
        return true;
    }

    /**
     * 修改用户密码。
     * @param userId  用户主键Id。
     * @param newPass 新密码。
     * @return 成功返回true，否则false。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean changePassword(Long userId, String newPass) {
        SysUser updatedUser = new SysUser();
        updatedUser.setUserId(userId);
        updatedUser.setPassword(passwordEncoder.encode(newPass));
        return sysUserMapper.updateById(updatedUser) == 1;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean changeHeadImage(Long userId, String newHeadImage) {
        SysUser updatedUser = new SysUser();
        updatedUser.setUserId(userId);
        updatedUser.setHeadImageUrl(newHeadImage);
        return sysUserMapper.updateById(updatedUser) == 1;
    }

    /**
     * 删除指定数据。
     *
     * @param userId 主键Id。
     * @return 成功返回true，否则false。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean remove(Long userId) {
        if (sysUserMapper.deleteById(userId) == 0) {
            return false;
        }
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(userId);
        sysUserRoleMapper.delete(new QueryWrapper<>(userRole));
        return true;
    }

    /**
     * 获取单表查询结果。由于没有关联数据查询，因此在仅仅获取单表数据的场景下，效率更高。
     * 如果需要同时获取关联数据，请移步(getSysUserListWithRelation)方法。
     *
     * @param filter  过滤对象。
     * @param orderBy 排序参数。
     * @return 查询结果集。
     */
    @Override
    public List<SysUser> getSysUserList(SysUser filter, String orderBy) {
        return sysUserMapper.getSysUserList(filter, orderBy);
    }

    /**
     * 获取主表的查询结果，以及主表关联的字典数据和一对一从表数据，以及一对一从表的字典数据。
     * 该查询会涉及到一对一从表的关联过滤，或一对多从表的嵌套关联过滤，因此性能不如单表过滤。
     * 如果仅仅需要获取主表数据，请移步(getSysUserList)，以便获取更好的查询性能。
     *
     * @param filter 主表过滤对象。
     * @param orderBy 排序参数。
     * @return 查询结果集。
     */
    @Override
    public List<SysUser> getSysUserListWithRelation(SysUser filter, String orderBy) {
        List<SysUser> resultList = sysUserMapper.getSysUserList(filter, orderBy);
        // 在缺省生成的代码中，如果查询结果resultList不是Page对象，说明没有分页，那么就很可能是数据导出接口调用了当前方法。
        // 为了避免一次性的大量数据关联，规避因此而造成的系统运行性能冲击，这里手动进行了分批次读取，开发者可按需修改该值。
        int batchSize = resultList instanceof Page ? 0 : 1000;
        this.buildRelationForDataList(resultList, MyRelationParam.normal(), batchSize);
        return resultList;
    }

    /**
     * 获取指定角色的用户列表。
     *
     * @param roleId  角色主键Id。
     * @param filter  用户过滤对象。
     * @param orderBy 排序参数。
     * @return 用户列表。
     */
    @Override
    public List<SysUser> getSysUserListByRoleId(Long roleId, SysUser filter, String orderBy) {
        return sysUserMapper.getSysUserListByRoleId(roleId, filter, orderBy);
    }

    /**
     * 获取不属于指定角色的用户列表。
     *
     * @param roleId  角色主键Id。
     * @param filter  用户过滤对象。
     * @param orderBy 排序参数。
     * @return 用户列表。
     */
    @Override
    public List<SysUser> getNotInSysUserListByRoleId(Long roleId, SysUser filter, String orderBy) {
        return sysUserMapper.getNotInSysUserListByRoleId(roleId, filter, orderBy);
    }

    /**
     * 查询用户的权限资源地址列表。同时返回详细的分配路径。
     *
     * @param userId 用户Id。
     * @param url    url过滤条件。
     * @return 包含从用户到权限资源的完整权限分配路径信息的查询结果列表。
     */
    @Override
    public List<Map<String, Object>> getSysPermListWithDetail(Long userId, String url) {
        return sysUserMapper.getSysPermListWithDetail(userId, url);
    }

    /**
     * 查询用户的权限字列表。同时返回详细的分配路径。
     *
     * @param userId   用户Id。
     * @param permCode 权限字名称过滤条件。
     * @return 包含从用户到权限字的权限分配路径信息的查询结果列表。
     */
    @Override
    public List<Map<String, Object>> getSysPermCodeListWithDetail(Long userId, String permCode) {
        return sysUserMapper.getSysPermCodeListWithDetail(userId, permCode);
    }

    /**
     * 查询用户的菜单列表。同时返回详细的分配路径。
     *
     * @param userId   用户Id。
     * @param menuName 菜单名称过滤条件。
     * @return 包含从用户到菜单的权限分配路径信息的查询结果列表。
     */
    @Override
    public List<Map<String, Object>> getSysMenuListWithDetail(Long userId, String menuName) {
        return sysUserMapper.getSysMenuListWithDetail(userId, menuName);
    }

    /**
     * 验证用户对象关联的数据是否都合法。
     *
     * @param sysUser         当前操作的对象。
     * @param originalSysUser 原有对象。
     * @param roleIds         逗号分隔的角色Id列表字符串。
     * @return 验证结果。
     */
    @Override
    public CallResult verifyRelatedData(SysUser sysUser, SysUser originalSysUser, String roleIds) {
        JSONObject jsonObject = new JSONObject();
        if (StrUtil.isBlank(roleIds)) {
            return CallResult.error("数据验证失败，用户的角色数据不能为空！");
        }
        Set<Long> roleIdSet = Arrays.stream(
                roleIds.split(",")).map(Long::valueOf).collect(Collectors.toSet());
        if (!sysRoleService.existAllPrimaryKeys(roleIdSet)) {
            return CallResult.error("数据验证失败，存在不合法的用户角色，请刷新后重试！");
        }
        jsonObject.put("roleIdSet", roleIdSet);
        //这里是基于字典的验证。
        if (this.needToVerify(sysUser, originalSysUser, SysUser::getDeptId)
                && !sysDeptService.existId(sysUser.getDeptId())) {
            return CallResult.error("数据验证失败，关联的用户部门Id并不存在，请刷新后重试！");
        }
        return CallResult.ok(jsonObject);
    }
}
