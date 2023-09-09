package com.bupt.common.online.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bupt.common.core.annotation.MyDataSourceResolver;
import com.bupt.common.core.base.dao.BaseDaoMapper;
import com.bupt.common.core.base.service.BaseService;
import com.bupt.common.core.constant.ApplicationConstant;
import com.bupt.common.core.constant.GlobalDeletedFlag;
import com.bupt.common.core.object.MyRelationParam;
import com.bupt.common.core.object.TokenData;
import com.bupt.common.core.util.MyModelUtil;
import com.bupt.common.core.util.DefaultDataSourceResolver;
import com.bupt.common.redis.util.CommonRedisUtil;
import com.bupt.common.sequence.wrapper.IdGeneratorWrapper;
import com.bupt.common.online.dao.OnlineColumnRuleMapper;
import com.bupt.common.online.dao.OnlineRuleMapper;
import com.bupt.common.online.model.OnlineColumnRule;
import com.bupt.common.online.model.OnlineRule;
import com.bupt.common.online.service.OnlineRuleService;
import com.github.pagehelper.Page;
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
import java.util.stream.Collectors;

/**
 * 验证规则数据操作服务类。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Slf4j
@MyDataSourceResolver(
        resolver = DefaultDataSourceResolver.class,
        intArg = ApplicationConstant.COMMON_FLOW_AND_ONLINE_DATASOURCE_TYPE)
@Service("onlineRuleService")
public class OnlineRuleServiceImpl extends BaseService<OnlineRule, Long> implements OnlineRuleService {

    @Autowired
    private OnlineRuleMapper onlineRuleMapper;
    @Autowired
    private OnlineColumnRuleMapper onlineColumnRuleMapper;
    @Autowired
    private IdGeneratorWrapper idGenerator;
    @Autowired
    private CommonRedisUtil commonRedisUtil;
    @Autowired
    private RedissonClient redissonClient;

    /**
     * 所有字段规则使用同一个键。
     */
    private static final String ONLINE_RULE_CACHE_KEY = "ONLINE_RULE";

    /**
     * 返回当前Service的主表Mapper对象。
     *
     * @return 主表Mapper对象。
     */
    @Override
    protected BaseDaoMapper<OnlineRule> mapper() {
        return onlineRuleMapper;
    }

    /**
     * 保存新增对象。
     *
     * @param onlineRule 新增对象。
     * @return 返回新增对象。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public OnlineRule saveNew(OnlineRule onlineRule) {
        commonRedisUtil.evictFormCache(ONLINE_RULE_CACHE_KEY);
        TokenData tokenData = TokenData.takeFromRequest();
        onlineRule.setRuleId(idGenerator.nextLongId());
        onlineRule.setAppCode(tokenData.getAppCode());
        Date now = new Date();
        onlineRule.setUpdateTime(now);
        onlineRule.setCreateTime(now);
        onlineRule.setCreateUserId(tokenData.getUserId());
        onlineRule.setUpdateUserId(tokenData.getUserId());
        onlineRule.setBuiltin(false);
        onlineRule.setDeletedFlag(GlobalDeletedFlag.NORMAL);
        MyModelUtil.setDefaultValue(onlineRule, "pattern", "");
        onlineRuleMapper.insert(onlineRule);
        return onlineRule;
    }

    /**
     * 更新数据对象。
     *
     * @param onlineRule         更新的对象。
     * @param originalOnlineRule 原有数据对象。
     * @return 成功返回true，否则false。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean update(OnlineRule onlineRule, OnlineRule originalOnlineRule) {
        commonRedisUtil.evictFormCache(ONLINE_RULE_CACHE_KEY);
        TokenData tokenData = TokenData.takeFromRequest();
        onlineRule.setAppCode(tokenData.getAppCode());
        onlineRule.setUpdateTime(new Date());
        onlineRule.setUpdateUserId(tokenData.getUserId());
        onlineRule.setCreateTime(originalOnlineRule.getCreateTime());
        onlineRule.setCreateUserId(originalOnlineRule.getCreateUserId());
        UpdateWrapper<OnlineRule> uw = this.createUpdateQueryForNullValue(onlineRule, onlineRule.getRuleId());
        return onlineRuleMapper.update(onlineRule, uw) == 1;
    }

    /**
     * 删除指定数据。
     *
     * @param ruleId 主键Id。
     * @return 成功返回true，否则false。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean remove(Long ruleId) {
        commonRedisUtil.evictFormCache(ONLINE_RULE_CACHE_KEY);
        if (onlineRuleMapper.deleteById(ruleId) == 0) {
            return false;
        }
        // 开始删除多对多父表的关联
        OnlineColumnRule onlineColumnRule = new OnlineColumnRule();
        onlineColumnRule.setRuleId(ruleId);
        onlineColumnRuleMapper.delete(new QueryWrapper<>(onlineColumnRule));
        return true;
    }

    /**
     * 获取单表查询结果。由于没有关联数据查询，因此在仅仅获取单表数据的场景下，效率更高。
     * 如果需要同时获取关联数据，请移步(getOnlineRuleListWithRelation)方法。
     *
     * @param filter  过滤对象。
     * @param orderBy 排序参数。
     * @return 查询结果集。
     */
    @Override
    public List<OnlineRule> getOnlineRuleList(OnlineRule filter, String orderBy) {
        if (filter == null) {
            filter = new OnlineRule();
        }
        filter.setAppCode(TokenData.takeFromRequest().getAppCode());
        return onlineRuleMapper.getOnlineRuleList(filter, orderBy);
    }

    /**
     * 获取主表的查询结果，以及主表关联的字典数据和一对一从表数据，以及一对一从表的字典数据。
     * 该查询会涉及到一对一从表的关联过滤，或一对多从表的嵌套关联过滤，因此性能不如单表过滤。
     * 如果仅仅需要获取主表数据，请移步(getOnlineRuleList)，以便获取更好的查询性能。
     *
     * @param filter  主表过滤对象。
     * @param orderBy 排序参数。
     * @return 查询结果集。
     */
    @Override
    public List<OnlineRule> getOnlineRuleListWithRelation(OnlineRule filter, String orderBy) {
        List<OnlineRule> resultList = this.getOnlineRuleList(filter, orderBy);
        // 在缺省生成的代码中，如果查询结果resultList不是Page对象，说明没有分页，那么就很可能是数据导出接口调用了当前方法。
        // 为了避免一次性的大量数据关联，规避因此而造成的系统运行性能冲击，这里手动进行了分批次读取，开发者可按需修改该值。
        int batchSize = resultList instanceof Page ? 0 : 1000;
        this.buildRelationForDataList(resultList, MyRelationParam.normal(), batchSize);
        return resultList;
    }

    /**
     * 在多对多关系中，当前Service的数据表为从表，返回不与指定主表主键Id存在对多对关系的列表。
     *
     * @param columnId 主表主键Id。
     * @param filter   从表的过滤对象。
     * @param orderBy  排序参数。
     * @return 查询结果集。
     */
    @Override
    public List<OnlineRule> getNotInOnlineRuleListByColumnId(Long columnId, OnlineRule filter, String orderBy) {
        if (filter == null) {
            filter = new OnlineRule();
        }
        filter.setAppCode(TokenData.takeFromRequest().getAppCode());
        List<OnlineRule> resultList =
                onlineRuleMapper.getNotInOnlineRuleListByColumnId(columnId, filter, orderBy);
        this.buildRelationForDataList(resultList, MyRelationParam.dictOnly());
        return resultList;
    }

    /**
     * 在多对多关系中，当前Service的数据表为从表，返回与指定主表主键Id存在对多对关系的列表。
     *
     * @param columnId 主表主键Id。
     * @param filter   从表的过滤对象。
     * @param orderBy  排序参数。
     * @return 查询结果集。
     */
    @Override
    public List<OnlineRule> getOnlineRuleListByColumnId(Long columnId, OnlineRule filter, String orderBy) {
        List<OnlineRule> resultList =
                onlineRuleMapper.getOnlineRuleListByColumnId(columnId, filter, orderBy);
        this.buildRelationForDataList(resultList, MyRelationParam.dictOnly());
        return resultList;
    }

    /**
     * 返回指定字段Id列表关联的字段规则对象列表。
     *
     * @param columnIdSet 指定的字段Id列表。
     * @return 关联的字段规则对象列表。
     */
    @Override
    public List<OnlineColumnRule> getOnlineColumnRuleListByColumnIds(Set<Long> columnIdSet) {
        LambdaQueryWrapper<OnlineColumnRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(OnlineColumnRule::getColumnId, columnIdSet);
        List<OnlineColumnRule> columnRuleList = onlineColumnRuleMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(columnRuleList)) {
            return columnRuleList;
        }
        List<OnlineRule> ruleList;
        RBucket<String> bucket = redissonClient.getBucket(ONLINE_RULE_CACHE_KEY);
        if (bucket.isExists()) {
            ruleList = JSONArray.parseArray(bucket.get(), OnlineRule.class);
        } else {
            ruleList = this.getAllList();
            if (CollUtil.isNotEmpty(ruleList)) {
                bucket.set(JSONArray.toJSONString(ruleList));
            }
        }
        if (CollUtil.isEmpty(ruleList)) {
            return columnRuleList;
        }
        Map<Long, OnlineRule> ruleMap = ruleList.stream().collect(Collectors.toMap(OnlineRule::getRuleId, c -> c));
        for (OnlineColumnRule columnRule : columnRuleList) {
            columnRule.setOnlineRule(ruleMap.get(columnRule.getRuleId()));
        }
        return columnRuleList;
    }
}
