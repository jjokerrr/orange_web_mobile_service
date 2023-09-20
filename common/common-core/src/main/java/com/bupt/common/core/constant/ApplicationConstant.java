package com.bupt.common.core.constant;

import java.util.regex.Pattern;

/**
 * 应用程序的常量声明对象。
 *
 * @author zzh
 * @date 2023-08-10
 */
public final class ApplicationConstant {

    /**
     * 适用于所有类型的字典格式数据。该常量为字典的键字段。
     */
    public static final String DICT_ID = "id";
    /**
     * 适用于所有类型的字典格式数据。该常量为字典的名称字段。
     */
    public static final String DICT_NAME = "name";
    /**
     * 适用于所有类型的字典格式数据。该常量为字典的键父字段。
     */
    public static final String PARENT_ID = "parentId";
    /**
     * 数据同步使用的缺省消息队列主题名称。
     */
    public static final String DEFAULT_DATA_SYNC_TOPIC = "flow_bupt";
    /**
     * 全量数据同步中，新增数据对象的键名称。
     */
    public static final String DEFAULT_FULL_SYNC_DATA_KEY = "data";
    /**
     * 全量数据同步中，原有数据对象的键名称。
     */
    public static final String DEFAULT_FULL_SYNC_OLD_DATA_KEY = "oldData";
    /**
     * 全量数据同步中，数据对象主键的键名称。
     */
    public static final String DEFAULT_FULL_SYNC_ID_KEY = "id";
    /**
     * 为字典表数据缓存时，缓存名称的固定后缀。
     */
    public static final String DICT_CACHE_NAME_SUFFIX = "-DICT";
    /**
     * 为树形字典表数据缓存时，缓存名称的固定后缀。
     */
    public static final String TREE_DICT_CACHE_NAME_SUFFIX = "-TREE-DICT";
    /**
     * 图片文件上传的父目录。
     */
    public static final String UPLOAD_IMAGE_PARENT_PATH = "image";
    /**
     * 附件文件上传的父目录。
     */
    public static final String UPLOAD_ATTACHMENT_PARENT_PATH = "attachment";
    /**
     * CSV文件扩展名。
     */
    public static final String CSV_EXT = "csv";
    /**
     * XLSX文件扩展名。
     */
    public static final String XLSX_EXT = "xlsx";
    /**
     * 统计分类计算时，按天聚合计算的常量值。(前端在MyOrderParam和MyGroupParam中传给后台)
     */
    public static final String DAY_AGGREGATION = "day";
    /**
     * 统计分类计算时，按月聚合计算的常量值。(前端在MyOrderParam和MyGroupParam中传给后台)
     */
    public static final String MONTH_AGGREGATION = "month";
    /**
     * 统计分类计算时，按年聚合计算的常量值。(前端在MyOrderParam和MyGroupParam中传给后台)
     */
    public static final String YEAR_AGGREGATION = "year";
    /**
     * 请求头跟踪id名。
     */
    public static final String HTTP_HEADER_TRACE_ID = "traceId";
    /**
     * 请求头菜单Id。
     */
    public static final String HTTP_HEADER_MENU_ID = "MenuId";
    /**
     * 数据权限中，标记所有菜单的Id值。
     */
    public static final String DATA_PERM_ALL_MENU_ID = "AllMenuId";
    /**
     * 请求头中记录的原始请求URL。
     */
    public static final String HTTP_HEADER_ORIGINAL_REQUEST_URL = "MY_ORIGINAL_REQUEST_URL";
    /**
     * 系统服务内部调用时，可使用该HEAD，以便和外部调用加以区分，便于监控和流量分析。
     */
    public static final String HTTP_HEADER_INTERNAL_TOKEN = "INTERNAL_AUTH_TOKEN";
    /**
     * 操作日志的数据源类型。
     */
    public static final int OPERATION_LOG_DATASOURCE_TYPE = 1000;
    /**
     * 在线表单的数据源类型。
     */
    public static final int COMMON_FLOW_AND_ONLINE_DATASOURCE_TYPE = 1010;
    /**
     * 报表模块的数据源类型。
     */
    public static final int COMMON_REPORT_DATASOURCE_TYPE = 1020;
    /**
     * 租户管理所对应的数据源常量值。
     */
    public static final int TENANT_ADMIN_DATASOURCE_TYPE = 1100;
    /**
     * 租户业务默认数据库(系统搭建时的第一个租户数据库)所对应的数据源常量值。
     */
    public static final int TENANT_BUSINESS_DATASOURCE_TYPE = 1120;
    /**
     * 租户通用数据所对应的数据源常量值，如全局编码字典、在线表单、流程和报表等内置表数据。
     */
    public static final int TENANT_COMMON_DATASOURCE_TYPE = 1130;
    /**
     * 租户动态数据源主题(Redis)。
     */
    public static final String TENANT_DYNAMIC_DATASOURCE_TOPIC = "TenantDynamicDatasoruce";
    /**
     * 租户基础数据同步(RocketMQ)，如upms、全局编码字典、在线表单、流程、报表等。
     */
    public static final String TENANT_DATASYNC_TOPIC = "TenantSync";
    /**
     * 重要说明：该值为项目生成后的缺省密钥，仅为使用户可以快速上手并跑通流程。
     * 在实际的应用中，一定要为不同的项目或服务，自行生成公钥和私钥，并将 PRIVATE_KEY 的引用改为服务的配置项。
     * 密钥的生成方式，可通过执行common.core.util.RsaUtil类的main函数动态生成。
     */
    public static final String PRIVATE_KEY =
            "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAKkLhAydtOtA4WuIkkIIUVaGWu4ElOEAQF9GTulHHWOwCHI1UvcKolvS1G+mdsKcmGtEAQ92AUde/kDRGu8Wn7kLDtCgUfo72soHz7Qfv5pVB4ohMxQd/9cxeKjKbDoirhB9Z3xGF20zUozp4ZPLxpTtI7azr0xzUtd5+D/HfLDrAgMBAAECgYEApESZhDz4YyeAJiPnpJ06lS8oS2VOWzsIUs0av5uoloeoHXtt7Lx7u2kroHeNrl3Hy2yg7ypH4dgQkGHin3VHrVAgjG3TxhgBXIqqntzzk2AGJKBeIIkRX86uTvtKZyp3flUgcwcGmpepAHS1V1DPY3aVYvbcqAmoL6DX6VYN0NECQQDQUitMdC76lEtAr5/ywS0nrZJDo6U7eQ7ywx/eiJ+YmrSye8oorlAj1VBWG+Cl6jdHOHtTQyYv/tu71fjzQiJTAkEAz7wb47/vcSUpNWQxItFpXz0o6rbJh71xmShn1AKP7XptOVZGlW9QRYEzHabV9m/DHqI00cMGhHrWZAhCiTkUCQJAFsJjaJ7o4weAkTieyO7B+CvGZw1h5/V55Jvcx3s1tH5yb22G0Jr6tm9/r2isSnQkReutzZLwgR3e886UvD7lcQJAAUcD2OOuQkDbPwPNtYwaHMbQgJj9JkOI9kskUE5vuiMdltOr/XFAyhygRtdmy2wmhAK1VnDfkmL6/IR8fEGImQJABOB0KCalb0M8CPnqqHzozrD8gPObnIIr4aVvLIPATN2g7MM2N6F7JbI4RZFiKa92LV6bhQCY8OvHi5K2cgFpbw==";
    /**
     * SQL注入检测的正则对象。
     */
    @SuppressWarnings("all")
    public static final Pattern SQL_INJECT_PATTERN =
            Pattern.compile("(.*\\=.*\\-\\-.*)|(.*(\\+).*)|(.*\\w+(%|\\$|#|&)\\w+.*)|(.*\\|\\|.*)|(.*\\s+(and|or)\\s+.*)" +
                    "|(.*\\b(select|update|union|and|or|delete|insert|trancate|char|substr|ascii|declare|exec|count|master|into|drop|execute|sleep|extractvalue|updatexml|substring|database|concat|rand)\\b.*)");

    /**
     * 私有构造函数，明确标识该常量类的作用。
     */
    private ApplicationConstant() {
    }
}
