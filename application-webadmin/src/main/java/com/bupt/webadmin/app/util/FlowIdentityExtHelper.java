package com.bupt.webadmin.app.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.bupt.common.flow.util.BaseFlowIdentityExtHelper;
import com.bupt.common.flow.util.FlowCustomExtFactory;
import com.bupt.common.flow.vo.FlowUserInfoVo;
import com.bupt.webadmin.upms.model.SysUser;
import com.bupt.webadmin.upms.model.constant.SysUserStatus;
import com.bupt.webadmin.upms.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 为流程提供所需的用户身份相关的等扩展信息的帮助类。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Slf4j
@Component
public class FlowIdentityExtHelper implements BaseFlowIdentityExtHelper {

    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private FlowCustomExtFactory flowCustomExtFactory;

    @PostConstruct
    public void doRegister() {
        flowCustomExtFactory.registerFlowIdentityExtHelper(this);
    }

    @Override
    public Set<String> getUsernameListByRoleIds(Set<String> roleIdSet) {
        Set<String> usernameSet = new HashSet<>();
        Set<Long> roleIdSet2 = roleIdSet.stream().map(Long::valueOf).collect(Collectors.toSet());
        SysUser filter = new SysUser();
        filter.setUserStatus(SysUserStatus.STATUS_NORMAL);
        for (Long roleId : roleIdSet2) {
            List<SysUser> userList = sysUserService.getSysUserListByRoleId(roleId, filter, null);
            this.extractAndAppendUsernameList(usernameSet, userList);
        }
        return usernameSet;
    }

    @Override
    public List<FlowUserInfoVo> getUserInfoListByRoleIds(Set<String> roleIdSet) {
        List<FlowUserInfoVo> resultList = new LinkedList<>();
        Set<Long> roleIdSet2 = roleIdSet.stream().map(Long::valueOf).collect(Collectors.toSet());
        SysUser filter = new SysUser();
        filter.setUserStatus(SysUserStatus.STATUS_NORMAL);
        for (Long roleId : roleIdSet2) {
            List<SysUser> userList = sysUserService.getSysUserListByRoleId(roleId, filter, null);
            if (CollUtil.isNotEmpty(userList)) {
                resultList.addAll(BeanUtil.copyToList(userList, FlowUserInfoVo.class));
            }
        }
        return resultList;
    }

    @Override
    public Set<String> getUsernameListByDeptIds(Set<String> deptIdSet) {
        Set<String> usernameSet = new HashSet<>();
        Set<Long> deptIdSet2 = deptIdSet.stream().map(Long::valueOf).collect(Collectors.toSet());
        for (Long deptId : deptIdSet2) {
            SysUser filter = new SysUser();
            filter.setDeptId(deptId);
            filter.setUserStatus(SysUserStatus.STATUS_NORMAL);
            List<SysUser> userList = sysUserService.getSysUserList(filter, null);
            this.extractAndAppendUsernameList(usernameSet, userList);
        }
        return usernameSet;
    }

    @Override
    public List<FlowUserInfoVo> getUserInfoListByDeptIds(Set<String> deptIdSet) {
        List<FlowUserInfoVo> resultList = new LinkedList<>();
        Set<Long> deptIdSet2 = deptIdSet.stream().map(Long::valueOf).collect(Collectors.toSet());
        for (Long deptId : deptIdSet2) {
            SysUser filter = new SysUser();
            filter.setDeptId(deptId);
            filter.setUserStatus(SysUserStatus.STATUS_NORMAL);
            List<SysUser> userList = sysUserService.getSysUserList(filter, null);
            if (CollUtil.isNotEmpty(userList)) {
                resultList.addAll(BeanUtil.copyToList(userList, FlowUserInfoVo.class));
            }
        }
        return resultList;
    }

    @Override
    public List<FlowUserInfoVo> getUserInfoListByUsernameSet(Set<String> usernameSet) {
        List<FlowUserInfoVo> resultList = null;
        List<SysUser> userList = sysUserService.getInList("loginName", usernameSet);
        if (CollUtil.isNotEmpty(userList)) {
            resultList = BeanUtil.copyToList(userList, FlowUserInfoVo.class);
        }
        return resultList;
    }

    @Override
    public Boolean supprtDataPerm() {
        return false;
    }

    @Override
    public Map<String, String> mapUserShowNameByLoginName(Set<String> loginNameSet) {
        if (CollUtil.isEmpty(loginNameSet)) {
            return new HashMap<>(1);
        }
        Map<String, String> resultMap = new HashMap<>(loginNameSet.size());
        List<SysUser> userList = sysUserService.getInList("loginName", loginNameSet);
        userList.forEach(user -> resultMap.put(user.getLoginName(), user.getShowName()));
        return resultMap;
    }

    private void extractAndAppendUsernameList(Set<String> resultUsernameList, List<SysUser> userList) {
        List<String> usernameList = userList.stream().map(SysUser::getLoginName).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(usernameList)) {
            resultUsernameList.addAll(usernameList);
        }
    }
}
