package com.bupt.webadmin.upms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.page.PageMethod;
import com.bupt.webadmin.upms.service.*;
import com.bupt.webadmin.upms.dao.*;
import com.bupt.webadmin.upms.model.*;
import com.bupt.common.ext.util.BizWidgetDatasourceExtHelper;
import com.bupt.common.ext.base.BizWidgetDatasource;
import com.bupt.common.ext.constant.BizWidgetDatasourceType;
import com.bupt.common.core.base.dao.BaseDaoMapper;
import com.bupt.common.core.constant.GlobalDeletedFlag;
import com.bupt.common.core.object.*;
import com.bupt.common.core.base.service.BaseService;
import com.bupt.common.core.util.MyModelUtil;
import com.bupt.common.core.util.MyPageUtil;
import com.bupt.common.sequence.wrapper.IdGeneratorWrapper;
import com.github.pagehelper.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 部门管理数据操作服务类。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Slf4j
@Service("sysDeptService")
public class SysDeptServiceImpl extends BaseService<SysDept, Long> implements SysDeptService, BizWidgetDatasource {

    @Autowired
    private SysDeptMapper sysDeptMapper;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private IdGeneratorWrapper idGenerator;
    @Autowired
    private BizWidgetDatasourceExtHelper bizWidgetDatasourceExtHelper;

    /**
     * 返回当前Service的主表Mapper对象。
     *
     * @return 主表Mapper对象。
     */
    @Override
    protected BaseDaoMapper<SysDept> mapper() {
        return sysDeptMapper;
    }

    @PostConstruct
    private void registerBizWidgetDatasource() {
        bizWidgetDatasourceExtHelper.registerDatasource(BizWidgetDatasourceType.UPMS_DEPT_TYPE, this);
    }

    @Override
    public MyPageData<Map<String, Object>> getDataList(
            String type, Map<String, Object> filter, MyOrderParam orderParam, MyPageParam pageParam) {
        if (pageParam != null) {
            PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize(), pageParam.getCount());
        }
        String orderBy = orderParam == null ? null : MyOrderParam.buildOrderBy(orderParam, SysDept.class);
        SysDept deptFilter = filter == null ? null : BeanUtil.toBean(filter, SysDept.class);
        List<SysDept> deptList = this.getSysDeptList(deptFilter, orderBy);
        this.buildRelationForDataList(deptList, MyRelationParam.dictOnly());
        return MyPageUtil.makeResponseData(deptList, BeanUtil::beanToMap);
    }

    @Override
    public List<Map<String, Object>> getDataListWithInList(String type, String fieldName, List<String> fieldValues) {
        List<SysDept> deptList;
        if (StrUtil.isBlank(fieldName)) {
            deptList = this.getInList(fieldValues.stream().map(Long::valueOf).collect(Collectors.toSet()));
        } else {
            deptList = this.getInList(fieldName, MyModelUtil.convertToTypeValues(SysDept.class, fieldName, fieldValues));
        }
        this.buildRelationForDataList(deptList, MyRelationParam.dictOnly());
        return MyModelUtil.beanToMapList(deptList);
    }

    /**
     * 保存新增的部门对象。
     *
     * @param sysDept 新增的部门对象。
     * @return 新增后的部门对象。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public SysDept saveNew(SysDept sysDept) {
        sysDept.setDeptId(idGenerator.nextLongId());
        sysDept.setDeletedFlag(GlobalDeletedFlag.NORMAL);
        MyModelUtil.fillCommonsForInsert(sysDept);
        sysDeptMapper.insert(sysDept);
        return sysDept;
    }

    /**
     * 更新部门对象。
     *
     * @param sysDept         更新的部门对象。
     * @param originalSysDept 原有的部门对象。
     * @return 更新成功返回true，否则false。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean update(SysDept sysDept, SysDept originalSysDept) {
        MyModelUtil.fillCommonsForUpdate(sysDept, originalSysDept);
        UpdateWrapper<SysDept> uw = this.createUpdateQueryForNullValue(sysDept, sysDept.getDeptId());
        return sysDeptMapper.update(sysDept, uw) != 0;
    }

    /**
     * 删除指定数据。
     *
     * @param deptId 主键Id。
     * @return 成功返回true，否则false。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean remove(Long deptId) {
        return sysDeptMapper.deleteById(deptId) == 1;
    }

    /**
     * 获取单表查询结果。由于没有关联数据查询，因此在仅仅获取单表数据的场景下，效率更高。
     * 如果需要同时获取关联数据，请移步(getSysDeptListWithRelation)方法。
     *
     * @param filter  过滤对象。
     * @param orderBy 排序参数。
     * @return 查询结果集。
     */
    @Override
    public List<SysDept> getSysDeptList(SysDept filter, String orderBy) {
        return sysDeptMapper.getSysDeptList(filter, orderBy);
    }

    /**
     * 获取主表的查询结果，以及主表关联的字典数据和一对一从表数据，以及一对一从表的字典数据。
     * 该查询会涉及到一对一从表的关联过滤，或一对多从表的嵌套关联过滤，因此性能不如单表过滤。
     * 如果仅仅需要获取主表数据，请移步(getSysDeptList)，以便获取更好的查询性能。
     *
     * @param filter 主表过滤对象。
     * @param orderBy 排序参数。
     * @return 查询结果集。
     */
    @Override
    public List<SysDept> getSysDeptListWithRelation(SysDept filter, String orderBy) {
        List<SysDept> resultList = sysDeptMapper.getSysDeptList(filter, orderBy);
        // 在缺省生成的代码中，如果查询结果resultList不是Page对象，说明没有分页，那么就很可能是数据导出接口调用了当前方法。
        // 为了避免一次性的大量数据关联，规避因此而造成的系统运行性能冲击，这里手动进行了分批次读取，开发者可按需修改该值。
        int batchSize = resultList instanceof Page ? 0 : 1000;
        this.buildRelationForDataList(resultList, MyRelationParam.normal(), batchSize);
        return resultList;
    }

    /**
     * 判断指定部门Id是否包含用户对象。
     *
     * @param deptId 部门主键Id。
     * @return 存在返回true，否则false。
     */
    @Override
    public boolean hasChildrenUser(Long deptId) {
        SysUser sysUser = new SysUser();
        sysUser.setDeptId(deptId);
        return sysUserService.getCountByFilter(sysUser) > 0;
    }
}
