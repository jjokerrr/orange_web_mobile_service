package com.bupt.common.online.object;

import com.alibaba.fastjson.annotation.JSONField;
import com.bupt.common.core.constant.ApplicationConstant;
import com.bupt.common.core.object.TokenData;
import com.bupt.common.core.util.ApplicationContextHolder;
import com.bupt.common.core.util.ContextUtil;
import com.bupt.common.sequence.wrapper.IdGeneratorWrapper;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * 跨库存储业务数据的事物性事件数据。
 *
 * @author zzh
 * @date 2023-08-10
 */
@NoArgsConstructor
@Data
public class TransactionalBusinessData {

    /**
     * 流水Id。
     */
    private Long transId;
    /**
     * 数据源Id。
     */
    private Long dblinkId;
    /**
     * 当前请求的url。
     */
    private String url;
    /**
     * 创建该事务性事件对象的初始方法。格式为：方法名(参数类型1,参数类型2)。
     */
    private String initMethod;
    /**
     * 当前请求的traceId。
     */
    private String traceId;
    /**
     * 操作描述。
     */
    private String operationDesc;
    /**
     * 处理错误信息。
     */
    private String errorReason;
    /**
     * 和SQL操作相关的数据。值类型为TransactionalEventSqlData对象。
     */
    private List<BusinessSqlData> sqlDataList = new LinkedList<>();
    /**
     * 创建者登录名。
     */
    private String createLoginName;
    /**
     * 创建者中文用户名。
     */
    private String createUsername;
    /**
     * 创建时间。
     */
    private Date createTime = new Date();

    @JSONField(serialize = false)
    private final IdGeneratorWrapper idGenerator =
            ApplicationContextHolder.getBean(IdGeneratorWrapper.class);

    protected static final String BUSINESS_DATA_ATTR_KEY = "transactionalBusinessData";

    public TransactionalBusinessData(HttpServletRequest request) {
        transId = idGenerator.nextLongId();
        url = request.getRequestURI();
        traceId = (String) request.getAttribute(ApplicationConstant.HTTP_HEADER_TRACE_ID);
        TokenData tokenData = TokenData.takeFromRequest();
        this.createUsername = tokenData.getShowName();
        this.createLoginName = tokenData.getLoginName();
    }

    /**
     * 获取HttpServletRequest属性中的事物性时间对象。
     *
     * @return 返回设置在当前请求属性中的事务性时间对象。如果不存在则返回NULL。
     */
    public static TransactionalBusinessData getFromRequestAttribute() {
        HttpServletRequest request = ContextUtil.getHttpRequest();
        return (TransactionalBusinessData) request.getAttribute(BUSINESS_DATA_ATTR_KEY);
    }

    /**
     * 获取HttpServletRequest属性中的事物性时间对象。如果存在直接返回，否则创建新对象，并设置到当前请求的指定属性中。
     *
     * @return 返回设置在当前请求属性中的事务性时间对象。
     */
    public static TransactionalBusinessData getOrCreateFromRequestAttribute() {
        HttpServletRequest request = ContextUtil.getHttpRequest();
        TransactionalBusinessData data = (TransactionalBusinessData) request.getAttribute(BUSINESS_DATA_ATTR_KEY);
        if (data != null) {
            return data;
        }
        data = new TransactionalBusinessData(request);
        request.setAttribute(BUSINESS_DATA_ATTR_KEY, data);
        return data;
    }

    /**
     * 从HttpServletRequest属性中移除该对象。
     */
    public static void removeFromRequestAttribute() {
        ContextUtil.getHttpRequest().removeAttribute(BUSINESS_DATA_ATTR_KEY);
    }

    /**
     * 获取事务监听器处理过程中的异常错误信息。
     *
     * @return 如果为null，则表示没有错误。
     */
    public static String getHandleErrorMessage() {
        TransactionalBusinessData data = getFromRequestAttribute();
        return data == null ? null : data.getErrorReason();
    }

    @Data
    @NoArgsConstructor
    public static class BusinessSqlData {

        /**
         * 表字段名称列表。主要用于插入。
         */
        private String sql;
        /**
         * 表字段值列表。主要用于插入。
         */
        private List<Serializable> columnValueList;

        public static BusinessSqlData createBy(String sql, List<Serializable> columnValueList) {
            BusinessSqlData o = new BusinessSqlData();
            o.sql= sql;
            o.columnValueList = columnValueList;
            return o;
        }
    }
}
