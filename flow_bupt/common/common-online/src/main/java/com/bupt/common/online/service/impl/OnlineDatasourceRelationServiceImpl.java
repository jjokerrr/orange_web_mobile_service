package com.bupt.common.online.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bupt.common.core.annotation.MyDataSourceResolver;
import com.bupt.common.core.base.dao.BaseDaoMapper;
import com.bupt.common.core.base.service.BaseService;
import com.bupt.common.core.constant.ApplicationConstant;
import com.bupt.common.core.object.CallResult;
import com.bupt.common.core.object.MyRelationParam;
import com.bupt.common.core.object.TokenData;
import com.bupt.common.core.util.DefaultDataSourceResolver;
import com.bupt.common.dbutil.object.SqlTable;
import com.bupt.common.dbutil.object.SqlTableColumn;
import com.bupt.common.redis.util.CommonRedisUtil;
import com.bupt.common.sequence.wrapper.IdGeneratorWrapper;
import com.bupt.common.online.util.OnlineRedisKeyUtil;
import com.bupt.common.online.dao.OnlineDatasourceRelationMapper;
import com.bupt.common.online.dao.OnlineDatasourceTableMapper;
import com.bupt.common.online.model.OnlineColumn;
import com.bupt.common.online.model.OnlineDatasourceRelation;
import com.bupt.common.online.model.OnlineDatasourceTable;
import com.bupt.common.online.model.OnlineTable;
import com.bupt.common.online.service.OnlineColumnService;
import com.bupt.common.online.service.OnlineDatasourceRelationService;
import com.bupt.common.online.service.OnlineDatasourceService;
import com.bupt.common.online.service.OnlineTableService;
import com.github.pagehelper.Page;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 数据源关联数据操作服务类。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Slf4j
@MyDataSourceResolver(
        resolver = DefaultDataSourceResolver.class,
        intArg = ApplicationConstant.COMMON_FLOW_AND_ONLINE_DATASOURCE_TYPE)
@Service("onlineDatasourceRelationService")
public class OnlineDatasourceRelationServiceImpl
        extends BaseService<OnlineDatasourceRelation, Long> implements OnlineDatasourceRelationService {

    @Autowired
    private OnlineDatasourceRelationMapper onlineDatasourceRelationMapper;
    @Autowired
    private OnlineDatasourceTableMapper onlineDatasourceTableMapper;
    @Autowired
    private OnlineDatasourceService onlineDatasourceService;
    @Autowired
    private OnlineColumnService onlineColumnService;
    @Autowired
    private OnlineTableService onlineTableService;
    @Autowired
    private IdGeneratorWrapper idGenerator;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private CommonRedisUtil commonRedisUtil;

    /**
     * 返回当前Service的主表Mapper对象。
     *
     * @return 主表Mapper对象。
     */
    @Override
    protected BaseDaoMapper<OnlineDatasourceRelation> mapper() {
        return onlineDatasourceRelationMapper;
    }

    /**
     * 保存新增对象。
     *
     * @param relation       新增对象。
     * @param slaveSqlTable  新增的关联从数据表对象。
     * @param slaveSqlColumn 新增的关联从数据表对象。
     * @return 返回新增对象。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public OnlineDatasourceRelation saveNew(
            OnlineDatasourceRelation relation, SqlTable slaveSqlTable, SqlTableColumn slaveSqlColumn) {
        commonRedisUtil.evictFormCache(OnlineRedisKeyUtil.makeOnlineDataSourceRelationKey(relation.getDatasourceId()));
        // 查找数据源关联的数据表，判断当前关联的从表，是否已经存在于zz_online_datasource_table中了。
        // 对于同一个数据源及其关联，同一个数据表只会被创建一次，如果已经和当前数据源的其他Relation，
        // 作为从表绑定了，怎么就可以直接使用这个OnlineTable了，否则就会为这个SqlTable，创建对应的OnlineTable。
        List<OnlineTable> datasourceTableList =
                onlineTableService.getOnlineTableListByDatasourceId(relation.getDatasourceId());
        OnlineTable relationSlaveTable = null;
        OnlineColumn relationSlaveColumn = null;
        for (OnlineTable onlineTable : datasourceTableList) {
            if (onlineTable.getTableName().equals(slaveSqlTable.getTableName())) {
                relationSlaveTable = onlineTable;
                relationSlaveColumn = onlineColumnService.getOnlineColumnByTableIdAndColumnName(
                        onlineTable.getTableId(), slaveSqlColumn.getColumnName());
                break;
            }
        }
        if (relationSlaveTable == null) {
            relationSlaveTable = onlineTableService.saveNewFromSqlTable(slaveSqlTable);
            for (OnlineColumn onlineColumn : relationSlaveTable.getColumnList()) {
                if (onlineColumn.getColumnName().equals(slaveSqlColumn.getColumnName())) {
                    relationSlaveColumn = onlineColumn;
                    break;
                }
            }
        }
        TokenData tokenData = TokenData.takeFromRequest();
        relation.setRelationId(idGenerator.nextLongId());
        relation.setAppCode(tokenData.getAppCode());
        relation.setSlaveTableId(relationSlaveTable.getTableId());
        relation.setSlaveColumnId(relationSlaveColumn == null ? null : relationSlaveColumn.getColumnId());
        Date now = new Date();
        relation.setUpdateTime(now);
        relation.setCreateTime(now);
        relation.setCreateUserId(tokenData.getUserId());
        relation.setUpdateUserId(tokenData.getUserId());
        onlineDatasourceRelationMapper.insert(relation);
        OnlineDatasourceTable datasourceTable = new OnlineDatasourceTable();
        datasourceTable.setId(idGenerator.nextLongId());
        datasourceTable.setDatasourceId(relation.getDatasourceId());
        datasourceTable.setRelationId(relation.getRelationId());
        datasourceTable.setTableId(relation.getSlaveTableId());
        onlineDatasourceTableMapper.insert(datasourceTable);
        return relation;
    }

    /**
     * 更新数据对象。
     *
     * @param relation         更新的对象。
     * @param originalRelation 原有数据对象。
     * @return 成功返回true，否则false。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean update(OnlineDatasourceRelation relation, OnlineDatasourceRelation originalRelation) {
        commonRedisUtil.evictFormCache(OnlineRedisKeyUtil.makeOnlineDataSourceRelationKey(relation.getDatasourceId()));
        TokenData tokenData = TokenData.takeFromRequest();
        relation.setAppCode(tokenData.getAppCode());
        relation.setUpdateTime(new Date());
        relation.setUpdateUserId(tokenData.getUserId());
        relation.setCreateTime(originalRelation.getCreateTime());
        relation.setCreateUserId(originalRelation.getCreateUserId());
        // 这里重点提示，在执行主表数据更新之前，如果有哪些字段不支持修改操作，请用原有数据对象字段替换当前数据字段。
        UpdateWrapper<OnlineDatasourceRelation> uw =
                this.createUpdateQueryForNullValue(relation, relation.getRelationId());
        return onlineDatasourceRelationMapper.update(relation, uw) == 1;
    }

    /**
     * 删除指定数据。
     *
     * @param relationId 主键Id。
     * @return 成功返回true，否则false。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean remove(Long relationId) {
        OnlineDatasourceRelation relation = this.getById(relationId);
        if (relation != null) {
            commonRedisUtil.evictFormCache(
                    OnlineRedisKeyUtil.makeOnlineDataSourceRelationKey(relation.getDatasourceId()));
        }
        if (onlineDatasourceRelationMapper.deleteById(relationId) != 1) {
            return false;
        }
        OnlineDatasourceTable filter = new OnlineDatasourceTable();
        filter.setRelationId(relationId);
        QueryWrapper<OnlineDatasourceTable> queryWrapper = new QueryWrapper<>(filter);
        OnlineDatasourceTable datasourceTable = onlineDatasourceTableMapper.selectOne(queryWrapper);
        onlineDatasourceTableMapper.delete(queryWrapper);
        filter = new OnlineDatasourceTable();
        filter.setDatasourceId(datasourceTable.getDatasourceId());
        filter.setTableId(datasourceTable.getTableId());
        // 不在有引用该表的时候，可以删除该数据源关联引用的从表了。
        if (onlineDatasourceTableMapper.selectCount(new QueryWrapper<>(filter)) == 0) {
            onlineTableService.remove(datasourceTable.getTableId());
        }
        return true;
    }

    /**
     * 当前服务的支持表为从表，根据主表的主键Id，删除一对多的从表数据。
     *
     * @param datasourceId 主表主键Id。
     * @return 删除数量。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int removeByDatasourceId(Long datasourceId) {
        commonRedisUtil.evictFormCache(OnlineRedisKeyUtil.makeOnlineDataSourceRelationKey(datasourceId));
        OnlineDatasourceRelation deletedObject = new OnlineDatasourceRelation();
        deletedObject.setDatasourceId(datasourceId);
        return onlineDatasourceRelationMapper.delete(new QueryWrapper<>(deletedObject));
    }

    @Override
    public List<OnlineDatasourceRelation> getOnlineDatasourceRelationListFromCache(Set<Long> datasourceIdSet) {
        List<OnlineDatasourceRelation> resultList = new LinkedList<>();
        datasourceIdSet.forEach(datasourceId -> {
            String key = OnlineRedisKeyUtil.makeOnlineDataSourceRelationKey(datasourceId);
            RBucket<String> bucket = redissonClient.getBucket(key);
            if (bucket.isExists()) {
                resultList.addAll(JSONArray.parseArray(bucket.get(), OnlineDatasourceRelation.class));
            } else {
                OnlineDatasourceRelation filter = new OnlineDatasourceRelation();
                filter.setDatasourceId(datasourceId);
                List<OnlineDatasourceRelation> relationList = this.getListByFilter(filter);
                if (CollUtil.isNotEmpty(relationList)) {
                    resultList.addAll(relationList);
                    bucket.set(JSONArray.toJSONString(relationList));
                }
            }
        });
        return resultList;
    }

    @Override
    public OnlineDatasourceRelation getOnlineDatasourceRelationFromCache(Long datasourceId, Long relationId) {
        List<OnlineDatasourceRelation> relationList =
                this.getOnlineDatasourceRelationListFromCache(CollUtil.newHashSet(datasourceId));
        if (CollUtil.isEmpty(relationList)) {
            return null;
        }
        return relationList.stream().filter(r -> r.getRelationId().equals(relationId)).findFirst().orElse(null);
    }

    /**
     * 获取主表的查询结果，以及主表关联的字典数据和一对一从表数据，以及一对一从表的字典数据。
     * 该查询会涉及到一对一从表的关联过滤，或一对多从表的嵌套关联过滤，因此性能不如单表过滤。
     * 如果仅仅需要获取主表数据，请移步(getOnlineDatasourceRelationList)，以便获取更好的查询性能。
     *
     * @param filter  主表过滤对象。
     * @param orderBy 排序参数。
     * @return 查询结果集。
     */
    @Override
    public List<OnlineDatasourceRelation> getOnlineDatasourceRelationListWithRelation(
            OnlineDatasourceRelation filter, String orderBy) {
        if (filter == null) {
            filter = new OnlineDatasourceRelation();
        }
        filter.setAppCode(TokenData.takeFromRequest().getAppCode());
        List<OnlineDatasourceRelation> resultList =
                onlineDatasourceRelationMapper.getOnlineDatasourceRelationList(filter, orderBy);
        // 在缺省生成的代码中，如果查询结果resultList不是Page对象，说明没有分页，那么就很可能是数据导出接口调用了当前方法。
        // 为了避免一次性的大量数据关联，规避因此而造成的系统运行性能冲击，这里手动进行了分批次读取，开发者可按需修改该值。
        int batchSize = resultList instanceof Page ? 0 : 1000;
        this.buildRelationForDataList(resultList, MyRelationParam.normal(), batchSize);
        return resultList;
    }

    /**
     * 根据最新对象和原有对象的数据对比，判断关联的字典数据和多对一主表数据是否都是合法数据。
     *
     * @param relation         最新数据对象。
     * @param originalRelation 原有数据对象。
     * @return 数据全部正确返回true，否则false。
     */
    @Override
    public CallResult verifyRelatedData(
            OnlineDatasourceRelation relation, OnlineDatasourceRelation originalRelation) {
        String errorMessageFormat = "数据验证失败，关联的%s并不存在，请刷新后重试！";
        if (this.needToVerify(relation, originalRelation, OnlineDatasourceRelation::getMasterColumnId)
                && !onlineColumnService.existId(relation.getMasterColumnId())) {
            return CallResult.error(String.format(errorMessageFormat, "主表关联字段Id"));
        }
        if (this.needToVerify(relation, originalRelation, OnlineDatasourceRelation::getSlaveTableId)
                && !onlineTableService.existId(relation.getSlaveTableId())) {
            return CallResult.error(String.format(errorMessageFormat, "从表Id"));
        }
        if (this.needToVerify(relation, originalRelation, OnlineDatasourceRelation::getSlaveColumnId)
                && !onlineColumnService.existId(relation.getSlaveColumnId())) {
            return CallResult.error(String.format(errorMessageFormat, "从表关联字段Id"));
        }
        return CallResult.ok();
    }
}
