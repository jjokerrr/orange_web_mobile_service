package com.bupt.webadmin.upms.dao;

import com.bupt.common.core.base.dao.BaseDaoMapper;
import com.bupt.webadmin.upms.model.SysPermModule;

import java.util.List;

/**
 * 权限资源模块数据访问操作接口。
 *
 * @author zzh
 * @date 2023-08-10
 */
public interface SysPermModuleMapper extends BaseDaoMapper<SysPermModule> {

    /**
     * 获取整个权限模块和权限关联后的全部数据。
     *
     * @return 关联的权限模块和权限资源列表。
     */
    List<SysPermModule> getPermModuleAndPermList();
}
