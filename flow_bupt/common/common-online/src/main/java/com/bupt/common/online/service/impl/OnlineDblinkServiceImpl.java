package com.bupt.common.online.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bupt.common.core.annotation.MyDataSourceResolver;
import com.bupt.common.core.base.dao.BaseDaoMapper;
import com.bupt.common.core.base.service.BaseService;
import com.bupt.common.core.constant.ApplicationConstant;
import com.bupt.common.core.object.MyRelationParam;
import com.bupt.common.core.object.TokenData;
import com.bupt.common.core.util.DefaultDataSourceResolver;
import com.bupt.common.dbutil.constant.DblinkType;
import com.bupt.common.dbutil.object.SqlTable;
import com.bupt.common.dbutil.object.SqlTableColumn;
import com.bupt.common.sequence.wrapper.IdGeneratorWrapper;
import com.bupt.common.online.config.OnlineProperties;
import com.bupt.common.online.dao.OnlineDblinkMapper;
import com.bupt.common.online.model.OnlineDblink;
import com.bupt.common.online.service.OnlineDblinkService;
import com.bupt.common.online.util.OnlineDataSourceUtil;
import com.github.pagehelper.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据库链接数据操作服务类。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Slf4j
@MyDataSourceResolver(
        resolver = DefaultDataSourceResolver.class,
        intArg = ApplicationConstant.COMMON_FLOW_AND_ONLINE_DATASOURCE_TYPE)
@Service("onlineDblinkService")
public class OnlineDblinkServiceImpl extends BaseService<OnlineDblink, Long> implements OnlineDblinkService {

    @Autowired
    private OnlineDblinkMapper onlineDblinkMapper;
    @Autowired
    private IdGeneratorWrapper idGenerator;
    @Autowired
    private OnlineProperties onlineProperties;
    @Autowired
    private OnlineDataSourceUtil dataSourceUtil;

    /**
     * 返回当前Service的主表Mapper对象。
     *
     * @return 主表Mapper对象。
     */
    @Override
    protected BaseDaoMapper<OnlineDblink> mapper() {
        return onlineDblinkMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public OnlineDblink saveNew(OnlineDblink onlineDblink) {
        onlineDblinkMapper.insert(this.buildDefaultValue(onlineDblink));
        return onlineDblink;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean update(OnlineDblink onlineDblink, OnlineDblink originalOnlineDblink) {
        if (!StrUtil.equals(onlineDblink.getConfiguration(), originalOnlineDblink.getConfiguration())) {
            dataSourceUtil.removeDataSource(onlineDblink.getDblinkId());
        }
        onlineDblink.setAppCode(TokenData.takeFromRequest().getAppCode());
        onlineDblink.setCreateUserId(originalOnlineDblink.getCreateUserId());
        onlineDblink.setUpdateUserId(TokenData.takeFromRequest().getUserId());
        onlineDblink.setCreateTime(originalOnlineDblink.getCreateTime());
        onlineDblink.setUpdateTime(new Date());
        // 这里重点提示，在执行主表数据更新之前，如果有哪些字段不支持修改操作，请用原有数据对象字段替换当前数据字段。
        UpdateWrapper<OnlineDblink> uw = this.createUpdateQueryForNullValue(onlineDblink, onlineDblink.getDblinkId());
        return onlineDblinkMapper.update(onlineDblink, uw) == 1;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean remove(Long dblinkId) {
        dataSourceUtil.removeDataSource(dblinkId);
        return onlineDblinkMapper.deleteById(dblinkId) == 1;
    }

    @Override
    public List<OnlineDblink> getOnlineDblinkList(OnlineDblink filter, String orderBy) {
        if (filter == null) {
            filter = new OnlineDblink();
        }
        filter.setAppCode(TokenData.takeFromRequest().getAppCode());
        return onlineDblinkMapper.getOnlineDblinkList(filter, orderBy);
    }

    @Override
    public List<OnlineDblink> getOnlineDblinkListWithRelation(OnlineDblink filter, String orderBy) {
        List<OnlineDblink> resultList = this.getOnlineDblinkList(filter, orderBy);
        // 在缺省生成的代码中，如果查询结果resultList不是Page对象，说明没有分页，那么就很可能是数据导出接口调用了当前方法。
        // 为了避免一次性的大量数据关联，规避因此而造成的系统运行性能冲击，这里手动进行了分批次读取，开发者可按需修改该值。
        int batchSize = resultList instanceof Page ? 0 : 1000;
        this.buildRelationForDataList(resultList, MyRelationParam.normal(), batchSize);
        return resultList;
    }

    @Override
    public List<SqlTable> getDblinkTableList(OnlineDblink dblink) {
        List<SqlTable> resultList = dataSourceUtil.getTableList(dblink.getDblinkId(), null);
        if (StrUtil.isNotBlank(onlineProperties.getTablePrefix())) {
            resultList = resultList.stream()
                    .filter(t -> StrUtil.startWith(t.getTableName(), onlineProperties.getTablePrefix()))
                    .collect(Collectors.toList());
        }
        resultList.forEach(t -> t.setDblinkId(dblink.getDblinkId()));
        return resultList;
    }

    @Override
    public SqlTable getDblinkTable(OnlineDblink dblink, String tableName) {
        SqlTable sqlTable = dataSourceUtil.getTable(dblink.getDblinkId(), tableName);
        sqlTable.setDblinkId(dblink.getDblinkId());
        sqlTable.setColumnList(getDblinkTableColumnList(dblink, tableName));
        return sqlTable;
    }

    @Override
    public List<SqlTableColumn> getDblinkTableColumnList(OnlineDblink dblink, String tableName) {
        List<SqlTableColumn> columnList = dataSourceUtil.getTableColumnList(dblink.getDblinkId(), tableName);
        columnList.forEach(c -> this.makeupSqlTableColumn(c, dblink.getDblinkType()));
        return columnList;
    }

    @Override
    public SqlTableColumn getDblinkTableColumn(OnlineDblink dblink, String tableName, String columnName) {
        List<SqlTableColumn> columnList = dataSourceUtil.getTableColumnList(dblink.getDblinkId(), tableName);
        SqlTableColumn sqlTableColumn = columnList.stream()
                .filter(c -> c.getColumnName().equals(columnName)).findFirst().orElse(null);
        if (sqlTableColumn != null) {
            this.makeupSqlTableColumn(sqlTableColumn, dblink.getDblinkType());
        }
        return sqlTableColumn;
    }

    private void makeupSqlTableColumn(SqlTableColumn sqlTableColumn, int dblinkType) {
        sqlTableColumn.setDblinkType(dblinkType);
        switch (dblinkType) {
            case DblinkType.POSTGRESQL:
                if (StrUtil.equalsAny(sqlTableColumn.getColumnType(), "char", "varchar")) {
                    sqlTableColumn.setFullColumnType(
                            sqlTableColumn.getColumnType() + "(" + sqlTableColumn.getStringPrecision() + ")");
                } else {
                    sqlTableColumn.setFullColumnType(sqlTableColumn.getColumnType());
                }
                break;
            case DblinkType.MYSQL:
                sqlTableColumn.setAutoIncrement("auto_increment".equals(sqlTableColumn.getExtra()));
                break;
            case DblinkType.ORACLE:
                if (StrUtil.equalsAny(sqlTableColumn.getColumnType(), "VARCHAR2", "NVARCHAR2", "CHAR", "NCHAR")) {
                    sqlTableColumn.setFullColumnType(
                            sqlTableColumn.getColumnType() + "(" + sqlTableColumn.getStringPrecision() + ")");
                } else if (StrUtil.equals(sqlTableColumn.getColumnType(), "NUMBER")) {
                    sqlTableColumn.setFullColumnType(sqlTableColumn.getColumnType() +
                            "(" + sqlTableColumn.getNumericPrecision() + "," + sqlTableColumn.getNumericScale() + ")");
                } else {
                    sqlTableColumn.setFullColumnType(sqlTableColumn.getColumnType());
                }
                break;
            case DblinkType.DAMENG:
            case DblinkType.KINGBASE:
                if (StrUtil.equalsAnyIgnoreCase(sqlTableColumn.getColumnType(), "VARCHAR", "VARCHAR2", "CHAR")) {
                    sqlTableColumn.setFullColumnType(
                            sqlTableColumn.getColumnType() + "(" + sqlTableColumn.getStringPrecision() + ")");
                } else if (StrUtil.equals(sqlTableColumn.getColumnType(), "NUMBER")) {
                    sqlTableColumn.setFullColumnType(sqlTableColumn.getColumnType() +
                            "(" + sqlTableColumn.getNumericPrecision() + "," + sqlTableColumn.getNumericScale() + ")");
                } else {
                    sqlTableColumn.setFullColumnType(sqlTableColumn.getColumnType());
                }
                break;
            default:
                break;
        }
    }

    private OnlineDblink buildDefaultValue(OnlineDblink onlineDblink) {
        onlineDblink.setDblinkId(idGenerator.nextLongId());
        TokenData tokenData = TokenData.takeFromRequest();
        onlineDblink.setCreateUserId(tokenData.getUserId());
        onlineDblink.setUpdateUserId(tokenData.getUserId());
        Date now = new Date();
        onlineDblink.setCreateTime(now);
        onlineDblink.setUpdateTime(now);
        onlineDblink.setAppCode(tokenData.getAppCode());
        return onlineDblink;
    }
}
