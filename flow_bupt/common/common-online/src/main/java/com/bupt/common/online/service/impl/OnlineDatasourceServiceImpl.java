package com.bupt.common.online.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bupt.common.core.annotation.MyDataSourceResolver;
import com.bupt.common.core.base.dao.BaseDaoMapper;
import com.bupt.common.core.base.service.BaseService;
import com.bupt.common.core.constant.ApplicationConstant;
import com.bupt.common.core.object.MyRelationParam;
import com.bupt.common.core.object.TokenData;
import com.bupt.common.core.util.DefaultDataSourceResolver;
import com.bupt.common.dbutil.object.SqlTable;
import com.bupt.common.redis.util.CommonRedisUtil;
import com.bupt.common.sequence.wrapper.IdGeneratorWrapper;
import com.bupt.common.online.dao.OnlineDatasourceMapper;
import com.bupt.common.online.dao.OnlineDatasourceTableMapper;
import com.bupt.common.online.dao.OnlinePageDatasourceMapper;
import com.bupt.common.online.model.OnlineDatasource;
import com.bupt.common.online.model.OnlineDatasourceTable;
import com.bupt.common.online.model.OnlinePageDatasource;
import com.bupt.common.online.model.OnlineTable;
import com.bupt.common.online.service.OnlineDatasourceRelationService;
import com.bupt.common.online.service.OnlineDatasourceService;
import com.bupt.common.online.service.OnlineTableService;
import com.bupt.common.online.util.OnlineRedisKeyUtil;
import com.github.pagehelper.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据模型数据操作服务类。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Slf4j
@MyDataSourceResolver(
        resolver = DefaultDataSourceResolver.class,
        intArg = ApplicationConstant.COMMON_FLOW_AND_ONLINE_DATASOURCE_TYPE)
@Service("onlineDatasourceService")
public class OnlineDatasourceServiceImpl extends BaseService<OnlineDatasource, Long> implements OnlineDatasourceService {

    @Autowired
    private OnlineDatasourceMapper onlineDatasourceMapper;
    @Autowired
    private OnlinePageDatasourceMapper onlinePageDatasourceMapper;
    @Autowired
    private OnlineDatasourceTableMapper onlineDatasourceTableMapper;
    @Autowired
    private OnlineTableService onlineTableService;
    @Autowired
    private OnlineDatasourceRelationService onlineDatasourceRelationService;
    @Autowired
    private IdGeneratorWrapper idGenerator;
    @Autowired
    private CommonRedisUtil commonRedisUtil;

    /**
     * 返回当前Service的主表Mapper对象。
     *
     * @return 主表Mapper对象。
     */
    @Override
    protected BaseDaoMapper<OnlineDatasource> mapper() {
        return onlineDatasourceMapper;
    }

    /**
     * 保存新增对象。
     *
     * @param onlineDatasource 新增对象。
     * @param sqlTable         新增的数据表对象。
     * @param pageId           关联的页面Id。
     * @return 返回新增对象。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public OnlineDatasource saveNew(OnlineDatasource onlineDatasource, SqlTable sqlTable, Long pageId) {
        TokenData tokenData = TokenData.takeFromRequest();
        OnlineTable onlineTable = onlineTableService.saveNewFromSqlTable(sqlTable);
        onlineDatasource.setDatasourceId(idGenerator.nextLongId());
        onlineDatasource.setAppCode(tokenData.getAppCode());
        onlineDatasource.setMasterTableId(onlineTable.getTableId());
        Date now = new Date();
        onlineDatasource.setUpdateTime(now);
        onlineDatasource.setCreateTime(now);
        onlineDatasource.setCreateUserId(tokenData.getUserId());
        onlineDatasource.setUpdateUserId(tokenData.getUserId());
        onlineDatasourceMapper.insert(onlineDatasource);
        OnlineDatasourceTable datasourceTable = new OnlineDatasourceTable();
        datasourceTable.setId(idGenerator.nextLongId());
        datasourceTable.setDatasourceId(onlineDatasource.getDatasourceId());
        datasourceTable.setTableId(onlineDatasource.getMasterTableId());
        onlineDatasourceTableMapper.insert(datasourceTable);
        OnlinePageDatasource onlinePageDatasource = new OnlinePageDatasource();
        onlinePageDatasource.setId(idGenerator.nextLongId());
        onlinePageDatasource.setPageId(pageId);
        onlinePageDatasource.setDatasourceId(onlineDatasource.getDatasourceId());
        onlinePageDatasourceMapper.insert(onlinePageDatasource);
        return onlineDatasource;
    }

    /**
     * 更新数据对象。
     *
     * @param onlineDatasource         更新的对象。
     * @param originalOnlineDatasource 原有数据对象。
     * @return 成功返回true，否则false。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean update(OnlineDatasource onlineDatasource, OnlineDatasource originalOnlineDatasource) {
        commonRedisUtil.evictFormCache(OnlineRedisKeyUtil.makeOnlineDataSourceKey(onlineDatasource.getDatasourceId()));
        TokenData tokenData = TokenData.takeFromRequest();
        onlineDatasource.setAppCode(tokenData.getAppCode());
        onlineDatasource.setUpdateTime(new Date());
        onlineDatasource.setUpdateUserId(tokenData.getUserId());
        onlineDatasource.setCreateTime(originalOnlineDatasource.getCreateTime());
        onlineDatasource.setCreateUserId(originalOnlineDatasource.getCreateUserId());
        // 这里重点提示，在执行主表数据更新之前，如果有哪些字段不支持修改操作，请用原有数据对象字段替换当前数据字段。
        UpdateWrapper<OnlineDatasource> uw =
                this.createUpdateQueryForNullValue(onlineDatasource, onlineDatasource.getDatasourceId());
        return onlineDatasourceMapper.update(onlineDatasource, uw) == 1;
    }

    /**
     * 删除指定数据。
     *
     * @param datasourceId 主键Id。
     * @return 成功返回true，否则false。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean remove(Long datasourceId) {
        commonRedisUtil.evictFormCache(OnlineRedisKeyUtil.makeOnlineDataSourceKey(datasourceId));
        if (onlineDatasourceMapper.deleteById(datasourceId) == 0) {
            return false;
        }
        onlineDatasourceRelationService.removeByDatasourceId(datasourceId);
        // 开始删除多对多父表的关联
        OnlinePageDatasource onlinePageDatasource = new OnlinePageDatasource();
        onlinePageDatasource.setDatasourceId(datasourceId);
        onlinePageDatasourceMapper.delete(new QueryWrapper<>(onlinePageDatasource));
        OnlineDatasourceTable filter = new OnlineDatasourceTable();
        filter.setDatasourceId(datasourceId);
        QueryWrapper<OnlineDatasourceTable> queryWrapper = new QueryWrapper<>(filter);
        List<OnlineDatasourceTable> datasourceTableList = onlineDatasourceTableMapper.selectList(queryWrapper);
        onlineDatasourceTableMapper.delete(queryWrapper);
        Set<Long> tableIdSet = datasourceTableList.stream()
                .map(OnlineDatasourceTable::getTableId).collect(Collectors.toSet());
        onlineTableService.removeByTableIdSet(tableIdSet);
        return true;
    }

    /**
     * 获取单表查询结果。由于没有关联数据查询，因此在仅仅获取单表数据的场景下，效率更高。
     * 如果需要同时获取关联数据，请移步(getOnlineDatasourceListWithRelation)方法。
     *
     * @param filter  过滤对象。
     * @param orderBy 排序参数。
     * @return 查询结果集。
     */
    @Override
    public List<OnlineDatasource> getOnlineDatasourceList(OnlineDatasource filter, String orderBy) {
        if (filter == null) {
            filter = new OnlineDatasource();
        }
        filter.setAppCode(TokenData.takeFromRequest().getAppCode());
        return onlineDatasourceMapper.getOnlineDatasourceList(filter, orderBy);
    }

    @Override
    public OnlineDatasource getOnlineDatasourceFromCache(Long datasourceId) {
        String key = OnlineRedisKeyUtil.makeOnlineDataSourceKey(datasourceId);
        return commonRedisUtil.getFromCache(key, datasourceId, this::getById, OnlineDatasource.class);
    }

    @Override
    public List<OnlineDatasource> getOnlineDatasourceListFromCache(Set<Long> datasourceIdSet) {
        List<OnlineDatasource> resultList = new LinkedList<>();
        datasourceIdSet.forEach(datasourceId -> resultList.add(this.getOnlineDatasourceFromCache(datasourceId)));
        return resultList;
    }

    /**
     * 获取主表的查询结果，以及主表关联的字典数据和一对一从表数据，以及一对一从表的字典数据。
     * 该查询会涉及到一对一从表的关联过滤，或一对多从表的嵌套关联过滤，因此性能不如单表过滤。
     * 如果仅仅需要获取主表数据，请移步(getOnlineDatasourceList)，以便获取更好的查询性能。
     *
     * @param filter  主表过滤对象。
     * @param orderBy 排序参数。
     * @return 查询结果集。
     */
    @Override
    public List<OnlineDatasource> getOnlineDatasourceListWithRelation(OnlineDatasource filter, String orderBy) {
        List<OnlineDatasource> resultList = this.getOnlineDatasourceList(filter, orderBy);
        // 在缺省生成的代码中，如果查询结果resultList不是Page对象，说明没有分页，那么就很可能是数据导出接口调用了当前方法。
        // 为了避免一次性的大量数据关联，规避因此而造成的系统运行性能冲击，这里手动进行了分批次读取，开发者可按需修改该值。
        int batchSize = resultList instanceof Page ? 0 : 1000;
        this.buildRelationForDataList(resultList, MyRelationParam.normal(), batchSize);
        return resultList;
    }

    /**
     * 在多对多关系中，当前Service的数据表为从表，返回与指定主表主键Id存在对多对关系的列表。
     *
     * @param pageId  主表主键Id。
     * @param filter  从表的过滤对象。
     * @param orderBy 排序参数。
     * @return 查询结果集。
     */
    @Override
    public List<OnlineDatasource> getOnlineDatasourceListByPageId(Long pageId, OnlineDatasource filter, String orderBy) {
        List<OnlineDatasource> resultList =
                onlineDatasourceMapper.getOnlineDatasourceListByPageId(pageId, filter, orderBy);
        this.buildRelationForDataList(resultList, MyRelationParam.dictOnly());
        return resultList;
    }

    /**
     * 获取指定数据源Id集合所关联的在线表关联数据。
     *
     * @param datasourceIdSet 数据源Id集合。
     * @return 数据源和数据表的多对多关联列表。
     */
    @Override
    public List<OnlineDatasourceTable> getOnlineDatasourceTableList(Set<Long> datasourceIdSet) {
        return onlineDatasourceTableMapper.selectList(new QueryWrapper<OnlineDatasourceTable>()
                .lambda().in(OnlineDatasourceTable::getDatasourceId, datasourceIdSet));
    }

    /**
     * 根据在线表单Id集合，获取关联的在线数据源对象列表。
     *
     * @param formIdSet 在线表单Id集合。
     * @return 与参数表单Id关联的数据源列表。
     */
    @Override
    public List<OnlineDatasource> getOnlineDatasourceListByFormIds(Set<Long> formIdSet) {
        return onlineDatasourceMapper.getOnlineDatasourceListByFormIds(formIdSet);
    }

    @Override
    public OnlineDatasource getOnlineDatasourceByMasterTableId(Long masterTableId) {
        return onlineDatasourceMapper.selectOne(
                new LambdaQueryWrapper<OnlineDatasource>().eq(OnlineDatasource::getMasterTableId, masterTableId));
    }

    @Override
    public boolean existByVariableName(String variableName) {
        OnlineDatasource filter = new OnlineDatasource();
        filter.setVariableName(variableName);
        return CollUtil.isNotEmpty(this.getOnlineDatasourceList(filter, null));
    }
    
    @Override
    public Map<Long, String> getPageIdAndVariableNameMapByPageIds(Set<Long> pageIds) {
        String ids = CollUtil.join(pageIds, ",");
        List<Map<String, Object>> dataList = onlineDatasourceMapper.getPageIdAndVariableNameMapByPageIds(ids);
        return dataList.stream()
                .collect(Collectors.toMap(c -> (Long) c.get("page_id"), c -> (String) c.get("variable_name")));
    }
}
