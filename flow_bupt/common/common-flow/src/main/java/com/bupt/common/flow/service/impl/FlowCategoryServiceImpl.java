package com.bupt.common.flow.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.Page;
import com.bupt.common.core.annotation.MyDataSourceResolver;
import com.bupt.common.core.base.dao.BaseDaoMapper;
import com.bupt.common.core.base.service.BaseService;
import com.bupt.common.core.constant.ApplicationConstant;
import com.bupt.common.core.object.MyRelationParam;
import com.bupt.common.core.object.TokenData;
import com.bupt.common.core.util.DefaultDataSourceResolver;
import com.bupt.common.sequence.wrapper.IdGeneratorWrapper;
import com.bupt.common.flow.dao.*;
import com.bupt.common.flow.model.*;
import com.bupt.common.flow.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Slf4j
@MyDataSourceResolver(
        resolver = DefaultDataSourceResolver.class,
        intArg = ApplicationConstant.COMMON_FLOW_AND_ONLINE_DATASOURCE_TYPE)
@Service("flowCategoryService")
public class FlowCategoryServiceImpl extends BaseService<FlowCategory, Long> implements FlowCategoryService {

    @Autowired
    private FlowCategoryMapper flowCategoryMapper;
    @Autowired
    private IdGeneratorWrapper idGenerator;

    /**
     * 返回当前Service的主表Mapper对象。
     *
     * @return 主表Mapper对象。
     */
    @Override
    protected BaseDaoMapper<FlowCategory> mapper() {
        return flowCategoryMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public FlowCategory saveNew(FlowCategory flowCategory) {
        flowCategory.setCategoryId(idGenerator.nextLongId());
        TokenData tokenData = TokenData.takeFromRequest();
        flowCategory.setAppCode(tokenData.getAppCode());
        flowCategory.setTenantId(tokenData.getTenantId());
        flowCategory.setUpdateUserId(tokenData.getUserId());
        flowCategory.setCreateUserId(tokenData.getUserId());
        Date now = new Date();
        flowCategory.setUpdateTime(now);
        flowCategory.setCreateTime(now);
        flowCategoryMapper.insert(flowCategory);
        return flowCategory;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean update(FlowCategory flowCategory, FlowCategory originalFlowCategory) {
        TokenData tokenData = TokenData.takeFromRequest();
        flowCategory.setAppCode(tokenData.getAppCode());
        flowCategory.setTenantId(tokenData.getTenantId());
        flowCategory.setUpdateUserId(tokenData.getUserId());
        flowCategory.setCreateUserId(originalFlowCategory.getCreateUserId());
        flowCategory.setUpdateTime(new Date());
        flowCategory.setCreateTime(originalFlowCategory.getCreateTime());
        // 这里重点提示，在执行主表数据更新之前，如果有哪些字段不支持修改操作，请用原有数据对象字段替换当前数据字段。
        UpdateWrapper<FlowCategory> uw =
                this.createUpdateQueryForNullValue(flowCategory, flowCategory.getCategoryId());
        return flowCategoryMapper.update(flowCategory, uw) == 1;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean remove(Long categoryId) {
        return flowCategoryMapper.deleteById(categoryId) == 1;
    }

    @Override
    public List<FlowCategory> getFlowCategoryList(FlowCategory filter, String orderBy) {
        if (filter == null) {
            filter = new FlowCategory();
        }
        TokenData tokenData = TokenData.takeFromRequest();
        filter.setTenantId(tokenData.getTenantId());
        filter.setAppCode(tokenData.getAppCode());
        return flowCategoryMapper.getFlowCategoryList(filter, orderBy);
    }

    @Override
    public List<FlowCategory> getFlowCategoryListWithRelation(FlowCategory filter, String orderBy) {
        List<FlowCategory> resultList = this.getFlowCategoryList(filter, orderBy);
        // 在缺省生成的代码中，如果查询结果resultList不是Page对象，说明没有分页，那么就很可能是数据导出接口调用了当前方法。
        // 为了避免一次性的大量数据关联，规避因此而造成的系统运行性能冲击，这里手动进行了分批次读取，开发者可按需修改该值。
        int batchSize = resultList instanceof Page ? 0 : 1000;
        this.buildRelationForDataList(resultList, MyRelationParam.normal(), batchSize);
        return resultList;
    }

    @Override
    public boolean existByCode(String code) {
        FlowCategory filter = new FlowCategory();
        filter.setCode(code);
        return CollUtil.isNotEmpty(this.getFlowCategoryList(filter, null));
    }
    
    @Override
    public List<FlowCategory> getInList(Set<Long> categoryIds) {
        LambdaQueryWrapper<FlowCategory> qw = new LambdaQueryWrapper<>();
        qw.in(FlowCategory::getCategoryId, categoryIds);
        TokenData tokenData = TokenData.takeFromRequest();
        if (tokenData.getAppCode() == null) {
            qw.isNull(FlowCategory::getAppCode);
        } else {
            qw.eq(FlowCategory::getAppCode, tokenData.getAppCode());
        }
        if (tokenData.getTenantId() == null) {
            qw.isNull(FlowCategory::getTenantId);
        } else {
            qw.eq(FlowCategory::getTenantId, tokenData.getTenantId());
        }
        return flowCategoryMapper.selectList(qw);
    }
}
