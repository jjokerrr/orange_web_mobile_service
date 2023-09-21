package com.bupt.common.online.util;

import com.bupt.common.core.exception.MyRuntimeException;
import com.bupt.common.dbutil.provider.DataSourceProvider;
import com.bupt.common.dbutil.util.DataSourceUtil;
import com.bupt.common.online.model.OnlineDblink;
import com.bupt.common.online.service.OnlineDblinkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 在线表单模块动态加载的数据源工具类。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Slf4j
@Component
public class OnlineDataSourceUtil extends DataSourceUtil {

    @Autowired
    private OnlineDblinkService dblinkService;

    @Override
    protected int getDblinkTypeByDblinkId(Long dblinkId) {
        DataSourceProvider provider = this.dblinkProviderMap.get(dblinkId);
        if (provider != null) {
            return provider.getDblinkType();
        }
        OnlineDblink dblink = dblinkService.getById(dblinkId);
        if (dblink == null) {
            throw new MyRuntimeException("Online DblinkId [" + dblinkId + "] doesn't exist!");
        }
        this.dblinkProviderMap.put(dblinkId, this.getProvider(dblink.getDblinkType()));
        return dblink.getDblinkType();
    }

    @Override
    protected String getDblinkConfigurationByDblinkId(Long dblinkId) {
        OnlineDblink dblink = dblinkService.getById(dblinkId);
        if (dblink == null) {
            throw new MyRuntimeException("Online DblinkId [" + dblinkId + "] doesn't exist!");
        }
        return dblink.getConfiguration();
    }
}
