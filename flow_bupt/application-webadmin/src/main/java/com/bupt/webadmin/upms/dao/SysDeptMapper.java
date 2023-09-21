package com.bupt.webadmin.upms.dao;

import com.bupt.common.core.base.dao.BaseDaoMapper;
import com.bupt.webadmin.upms.model.SysDept;
import org.apache.ibatis.annotations.Param;

import java.util.*;

/**
 * 部门管理数据操作访问接口。
 *
 * @author zzh
 * @date 2023-08-10
 */
public interface SysDeptMapper extends BaseDaoMapper<SysDept> {

    /**
     * 批量插入对象列表。
     *
     * @param sysDeptList 新增对象列表。
     */
    void insertList(List<SysDept> sysDeptList);

    /**
     * 获取过滤后的对象列表。
     *
     * @param sysDeptFilter 主表过滤对象。
     * @param orderBy 排序字符串，order by从句的参数。
     * @return 对象列表。
     */
    List<SysDept> getSysDeptList(
            @Param("sysDeptFilter") SysDept sysDeptFilter, @Param("orderBy") String orderBy);
}
