package com.bupt.webadmin.app.service.impl;

import com.bupt.webadmin.app.service.AreaCodeService;
import com.bupt.webadmin.app.dao.AreaCodeMapper;
import com.bupt.webadmin.app.model.AreaCode;
import com.bupt.common.core.cache.MapTreeDictionaryCache;
import com.bupt.common.core.base.service.BaseDictService;
import com.bupt.common.core.base.dao.BaseDaoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * 行政区划的Service类。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Service("areaCodeService")
public class AreaCodeServiceImpl extends BaseDictService<AreaCode, Long> implements AreaCodeService {

    @Autowired
    private AreaCodeMapper areaCodeMapper;

    public AreaCodeServiceImpl() {
        super();
        this.dictionaryCache = MapTreeDictionaryCache.create(AreaCode::getAreaId, AreaCode::getParentId);
    }

    @PostConstruct
    public void init() {
        this.reloadCachedData(true);
    }

    @Override
    protected BaseDaoMapper<AreaCode> mapper() {
        return areaCodeMapper;
    }
}
