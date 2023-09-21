package com.bupt.common.online.service;

import com.alibaba.fastjson.JSONObject;
import com.bupt.common.core.object.MyPageData;
import com.bupt.common.core.object.MyPageParam;
import com.bupt.common.online.dto.OnlineFilterDto;
import com.bupt.common.online.model.OnlineColumn;
import com.bupt.common.online.model.OnlineDatasourceRelation;
import com.bupt.common.online.model.OnlineDict;
import com.bupt.common.online.model.OnlineTable;
import com.bupt.common.online.object.TransactionalBusinessData;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 在线表单运行时操作的数据服务接口。
 *
 * @author zzh
 * @date 2023-08-10
 */
public interface OnlineOperationService {

    /**
     * 待插入的所有表数据。
     *
     * @param table 在线表对象。
     * @param data  数据对象。
     * @return 主键值。由于自增主键不能获取插入后的主键值，因此返回NULL。
     */
    Object saveNew(OnlineTable table, JSONObject data);

    /**
     * 待插入的主表数据和多个从表数据。
     *
     * @param masterTable      主表在线表对象。
     * @param masterData       主表数据对象。
     * @param slaveDataListMap 多个从表的数据字段数据。
     * @return 主表的主键值。由于自增主键不能获取插入后的主键值，因此返回NULL。
     */
    Object saveNewWithRelation(
            OnlineTable masterTable,
            JSONObject masterData,
            Map<OnlineDatasourceRelation, List<JSONObject>> slaveDataListMap);

    /**
     * 更新表数据。
     *
     * @param table 在线表对象。
     * @param data  单条表数据。
     * @return true 更新成功，否则false。
     */
    boolean update(OnlineTable table, JSONObject data);

    /**
     * 更新流程字段的状态。
     *
     * @param table     数据表。
     * @param dataId    主键Id。
     * @param column    更新字段。
     * @param dataValue 新的数据值。
     * @return true 更新成功，否则false。
     */
    <T> boolean updateColumn(OnlineTable table, String dataId, OnlineColumn column, T dataValue);

    /**
     * 级联更新主表和从表数据。
     *
     * @param masterTable  主表对象。
     * @param masterData   主表数据。
     * @param datasourceId 主表数据源Id。
     * @param slaveData    关联从表数据。
     */
    void updateWithRelation(OnlineTable masterTable, JSONObject masterData, Long datasourceId, JSONObject slaveData);

    /**
     * 更新关联从表的数据。
     *
     * @param masterTable  主表对象。
     * @param masterData   主表数据。
     * @param masterDataId 主表主键Id。
     * @param datasourceId 主表数据源Id。
     * @param relationId   关联Id。
     * @param slaveData    从表数据。
     */
    void updateRelationData(
            OnlineTable masterTable,
            Map<String, Object> masterData,
            String masterDataId,
            Long datasourceId,
            Long relationId,
            Object slaveData);

    /**
     * 删除主表数据，及其需要级联删除的一对多关联从表数据。
     *
     * @param table        表对象。
     * @param relationList 一对多关联对象列表。
     * @param dataId       主表主键Id值。
     * @return true 删除成功，否则false。
     */
    boolean delete(OnlineTable table, List<OnlineDatasourceRelation> relationList, String dataId);

    /**
     * 删除一对多从表数据中的关联数据。
     * 删除所有字段为slaveColumn，数据值为columnValue，但是主键值不在keptIdSet中的从表关联数据。
     *
     * @param slaveTable  一对多从表。
     * @param slaveColumn 从表关联字段。
     * @param columnValue 关联字段的值。
     * @param keptIdSet   被保留从表数据的主键Id值。
     */
    void deleteOneToManySlaveData(
            OnlineTable slaveTable, OnlineColumn slaveColumn, String columnValue, Set<String> keptIdSet);

    /**
     * 从数据源和一对一数据源关联中，动态获取数据。
     *
     * @param table                主表对象。
     * @param oneToOneRelationList 数据源一对一关联列表。
     * @param allRelationList      数据源全部关联列表。
     * @param dataId               主表主键Id值。
     * @return 查询结果。
     */
    Map<String, Object> getMasterData(
            OnlineTable table,
            List<OnlineDatasourceRelation> oneToOneRelationList,
            List<OnlineDatasourceRelation> allRelationList,
            String dataId);

    /**
     * 从一对多数据源关联中，动态获取数据。
     *
     * @param relation 一对多数据源关联对象。
     * @param dataId   一对多关联数据主键Id值。
     * @return 查询结果。
     */
    Map<String, Object> getSlaveData(OnlineDatasourceRelation relation, String dataId);

    /**
     * 从数据源和一对一数据源关联中，动态获取数据列表。
     *
     * @param table                主表对象。
     * @param oneToOneRelationList 数据源一对一关联列表。
     * @param allRelationList      数据源全部关联列表。
     * @param filterList           过滤参数列表。
     * @param orderBy              排序字符串。
     * @param pageParam            分页对象。
     * @return 查询结果集。
     */
    MyPageData<Map<String, Object>> getMasterDataList(
            OnlineTable table,
            List<OnlineDatasourceRelation> oneToOneRelationList,
            List<OnlineDatasourceRelation> allRelationList,
            List<OnlineFilterDto> filterList,
            String orderBy,
            MyPageParam pageParam);

    /**
     * 从一对多数据源关联中，动态获取数据列表。
     *
     * @param relation   一对多数据源关联对象。
     * @param filterList 过滤参数列表。
     * @param orderBy    排序字符串。
     * @param pageParam  分页对象。
     * @return 查询结果集。
     */
    MyPageData<Map<String, Object>> getSlaveDataList(
            OnlineDatasourceRelation relation, List<OnlineFilterDto> filterList, String orderBy, MyPageParam pageParam);

    /**
     * 从字典对象指向的数据表中查询数据，并根据参数进行数据过滤。
     *
     * @param dict       字典对象。
     * @param filterList 过滤参数列表。
     * @return 查询结果集。
     */
    List<Map<String, Object>> getDictDataList(OnlineDict dict, List<OnlineFilterDto> filterList);

    /**
     * 为主表及其关联表数据绑定字典数据。
     *
     * @param masterTable  主表对象。
     * @param relationList 主表依赖的关联列表。
     * @param dataList     数据列表。
     */
    void buildDataListWithDict(
            OnlineTable masterTable, List<OnlineDatasourceRelation> relationList, List<Map<String, Object>> dataList);

    /**
     * 为字段类型是MASK_FIELD的字段执行脱敏计算。
     *
     * @param table          在线表单数据表对象。
     * @param data           数据对象。
     * @param ignoreFieldSet 忽略的字段名集合。
     */
    void maskFieldData(OnlineTable table, Map<String, Object> data, Set<String> ignoreFieldSet);

    /**
     * 为字段类型是MASK_FIELD的字段执行脱敏计算。
     *
     * @param table          在线表单数据表对象。
     * @param dataList       数据列表。
     * @param ignoreFieldSet 忽略的字段名集合。
     */
    void maskFieldDataList(OnlineTable table, List<Map<String, Object>> dataList, Set<String> ignoreFieldSet);

    /**
     * 批量处理在线表单的业务数据。
     *
     * @param businessData 业务数据对象。
     */
    void bulkHandleBusinessData(TransactionalBusinessData businessData);

    /**
     * 获取在线表单所关联的权限数据，包括权限字列表和权限资源列表。
     *
     * @param menuFormIds 菜单关联的表单Id集合。
     * @param viewFormIds 查询权限的表单Id集合。
     * @param editFormIds 编辑权限的表单Id集合。
     * @return 在线表单权限数据。
     */
    Map<String, Object> calculatePermData(Set<Long> menuFormIds, Set<Long> viewFormIds, Set<Long> editFormIds);

    /**
     * 重新结算指定表字段的自动编码计数器值，并将计算结果同步到Redis。
     *
     * @param tableId  表Id。
     * @param columnId 字段Id。如果该字段没有配置字段编码规则，该方法将直接返回。
     */
    void recalculateColumnRuleCode(Long tableId, Long columnId);
}
