package com.bupt.common.online.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bupt.common.core.annotation.MyDataSourceResolver;
import com.bupt.common.core.base.dao.BaseDaoMapper;
import com.bupt.common.core.base.service.BaseService;
import com.bupt.common.core.constant.ApplicationConstant;
import com.bupt.common.core.object.MyRelationParam;
import com.bupt.common.core.object.TokenData;
import com.bupt.common.core.util.DefaultDataSourceResolver;
import com.bupt.common.dbutil.object.SqlTable;
import com.bupt.common.sequence.wrapper.IdGeneratorWrapper;
import com.bupt.common.online.dao.OnlineTableMapper;
import com.bupt.common.online.model.OnlineColumn;
import com.bupt.common.online.model.OnlineTable;
import com.bupt.common.online.model.constant.FieldKind;
import com.bupt.common.online.service.OnlineColumnService;
import com.bupt.common.online.service.OnlineTableService;
import com.bupt.common.online.util.OnlineRedisKeyUtil;
import com.google.common.base.CaseFormat;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 数据表数据操作服务类。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Slf4j
@MyDataSourceResolver(
        resolver = DefaultDataSourceResolver.class,
        intArg = ApplicationConstant.COMMON_FLOW_AND_ONLINE_DATASOURCE_TYPE)
@Service("onlineTableService")
public class OnlineTableServiceImpl extends BaseService<OnlineTable, Long> implements OnlineTableService {

    @Autowired
    private OnlineTableMapper onlineTableMapper;
    @Autowired
    private OnlineColumnService onlineColumnService;
    @Autowired
    private IdGeneratorWrapper idGenerator;
    @Autowired
    private RedissonClient redissonClient;

    /**
     * 在线对象表的缺省缓存时间(小时)。
     */
    private static final int DEFAULT_CACHED_TABLE_HOURS = 168;

    /**
     * 返回当前Service的主表Mapper对象。
     *
     * @return 主表Mapper对象。
     */
    @Override
    protected BaseDaoMapper<OnlineTable> mapper() {
        return onlineTableMapper;
    }

    /**
     * 基于数据库表保存新增对象。
     *
     * @param sqlTable 数据库表对象。
     * @return 返回新增对象。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public OnlineTable saveNewFromSqlTable(SqlTable sqlTable) {
        OnlineTable onlineTable = new OnlineTable();
        TokenData tokenData = TokenData.takeFromRequest();
        onlineTable.setAppCode(tokenData.getAppCode());
        onlineTable.setDblinkId(sqlTable.getDblinkId());
        onlineTable.setTableId(idGenerator.nextLongId());
        onlineTable.setTableName(sqlTable.getTableName());
        String modelName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, sqlTable.getTableName());
        onlineTable.setModelName(modelName);
        Date now = new Date();
        onlineTable.setUpdateTime(now);
        onlineTable.setCreateTime(now);
        onlineTable.setCreateUserId(tokenData.getUserId());
        onlineTable.setUpdateUserId(tokenData.getUserId());
        onlineTableMapper.insert(onlineTable);
        List<OnlineColumn> columnList = onlineColumnService.saveNewList(sqlTable.getColumnList(), onlineTable.getTableId());
        onlineTable.setColumnList(columnList);
        return onlineTable;
    }

    /**
     * 删除指定表及其关联的字段数据。
     *
     * @param tableId 主键Id。
     * @return 成功返回true，否则false。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean remove(Long tableId) {
        if (onlineTableMapper.deleteById(tableId) == 0) {
            return false;
        }
        this.evictTableCache(tableId);
        onlineColumnService.removeByTableId(tableId);
        return true;
    }

    /**
     * 删除指定数据表Id集合中的表，及其关联字段。
     *
     * @param tableIdSet 待删除的数据表Id集合。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeByTableIdSet(Set<Long> tableIdSet) {
        tableIdSet.forEach(this::evictTableCache);
        onlineTableMapper.delete(
                new QueryWrapper<OnlineTable>().lambda().in(OnlineTable::getTableId, tableIdSet));
        onlineColumnService.removeByTableIdSet(tableIdSet);
    }

    /**
     * 根据数据源Id，获取该数据源及其关联所引用的数据表列表。
     *
     * @param datasourceId 指定的数据源Id。
     * @return 该数据源及其关联所引用的数据表列表。
     */
    @Override
    public List<OnlineTable> getOnlineTableListByDatasourceId(Long datasourceId) {
        return onlineTableMapper.getOnlineTableListByDatasourceId(datasourceId);
    }

    /**
     * 从缓存中获取指定的表数据及其关联字段列表。优先从缓存中读取，如果不存在则从数据库中读取，并同步到缓存。
     * 该接口方法仅仅用户在线表单的动态数据操作接口，而非在线表单的配置接口。
     *
     * @param tableId 表主键Id。
     * @return 查询后的在线表对象。
     */
    @Override
    public OnlineTable getOnlineTableFromCache(Long tableId) {
        String redisKey = OnlineRedisKeyUtil.makeOnlineTableKey(tableId);
        RBucket<String> tableBucket = redissonClient.getBucket(redisKey);
        if (tableBucket.isExists()) {
            String tableInfo = tableBucket.get();
            return JSON.parseObject(tableInfo, OnlineTable.class);
        }
        OnlineTable table = this.getByIdWithRelation(tableId, MyRelationParam.full());
        if (table == null) {
            return null;
        }
        for (OnlineColumn column : table.getColumnList()) {
            if (BooleanUtil.isTrue(column.getPrimaryKey())) {
                table.setPrimaryKeyColumn(column);
                continue;
            }
            if (ObjectUtil.equal(column.getFieldKind(), FieldKind.LOGIC_DELETE)) {
                table.setLogicDeleteColumn(column);
            }
        }
        Map<Long, OnlineColumn> columnMap =
                table.getColumnList().stream().collect(Collectors.toMap(OnlineColumn::getColumnId, c -> c));
        table.setColumnMap(columnMap);
        table.setColumnList(null);
        tableBucket.set(JSON.toJSONString(table));
        tableBucket.expire(DEFAULT_CACHED_TABLE_HOURS, TimeUnit.HOURS);
        return table;
    }

    @Override
    public OnlineColumn getOnlineColumnFromCache(Long tableId, Long columnId) {
        OnlineTable table = this.getOnlineTableFromCache(tableId);
        if (table == null) {
            return null;
        }
        return table.getColumnMap().get(columnId);
    }

    private void evictTableCache(Long tableId) {
        String tableIdKey = OnlineRedisKeyUtil.makeOnlineTableKey(tableId);
        redissonClient.getBucket(tableIdKey).delete();
    }
}
