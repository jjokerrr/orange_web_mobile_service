SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 在线表单字段表
-- ----------------------------
DROP TABLE IF EXISTS `zz_online_column`;
CREATE TABLE `zz_online_column` (
  `column_id` bigint(20) NOT NULL COMMENT '主键Id',
  `column_name` varchar(64) COLLATE utf8mb4_bin NOT NULL COMMENT '字段名',
  `table_id` bigint(20) NOT NULL COMMENT '数据表Id',
  `column_type` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '数据表中的字段类型',
  `full_column_type` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '数据表中的完整字段类型(包括了精度和刻度)',
  `primary_key` bit(1) NOT NULL COMMENT '是否为主键',
  `auto_incr` bit(1) NOT NULL COMMENT '是否是自增主键(0: 不是 1: 是)',
  `nullable` bit(1) NOT NULL COMMENT '是否可以为空 (0: 不可以为空 1: 可以为空)',
  `column_default` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '缺省值',
  `column_show_order` int(11) NOT NULL COMMENT '字段在数据表中的显示位置',
  `column_comment` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '数据表中的字段注释',
  `object_field_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '对象映射字段名称',
  `object_field_type` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '对象映射字段类型',
  `numeric_precision` int(11) COMMENT '数值型字段的精度',
  `numeric_scale` int(11) COMMENT '数值型字段的刻度',
  `filter_type` int(11) NOT NULL DEFAULT 1 COMMENT '字段过滤类型',
  `parent_key` bit(1) NOT NULL COMMENT '是否是主键的父Id',
  `dept_filter` bit(1) NOT NULL COMMENT '是否部门过滤字段',
  `user_filter` bit(1) NOT NULL COMMENT '是否用户过滤字段',
  `field_kind` int(11) DEFAULT NULL COMMENT '字段类别',
  `max_file_count` int(11) DEFAULT NULL COMMENT '包含的文件文件数量，0表示无限制',
  `upload_file_system_type` int(11) DEFAULT 0 COMMENT '上传文件系统类型',
  `encoded_rule` varchar(255) DEFAULT NULL COMMENT '编码规则的JSON格式数据',
  `mask_field_type` varchar(64) DEFAULT NULL COMMENT '脱敏字段类型',
  `dict_id` bigint(20) DEFAULT NULL COMMENT '字典Id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_user_id` bigint NOT NULL COMMENT '创建者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `update_user_id` bigint NOT NULL COMMENT '更新者',
  PRIMARY KEY (`column_id`),
  KEY `idx_table_id` (`table_id`) USING BTREE,
  KEY `idx_dict_id` (`dict_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='在线表单字段表';

-- ----------------------------
-- 在线表单字段和字段规则关联中间表
-- ----------------------------
DROP TABLE IF EXISTS `zz_online_column_rule`;
CREATE TABLE `zz_online_column_rule` (
  `column_id` bigint(20) NOT NULL COMMENT '字段Id',
  `rule_id` bigint(20) NOT NULL COMMENT '规则Id',
  `prop_data_json` text COLLATE utf8mb4_bin COMMENT '规则属性数据',
  PRIMARY KEY (`column_id`,`rule_id`) USING BTREE,
  KEY `idx_rule_id` (`rule_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='在线表单字段和字段规则关联中间表';

-- ----------------------------
-- 在线表单数据源表
-- ----------------------------
DROP TABLE IF EXISTS `zz_online_datasource`;
CREATE TABLE `zz_online_datasource` (
  `datasource_id` bigint(20) NOT NULL COMMENT '主键Id',
  `app_code` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '应用编码',
  `datasource_name` varchar(64) COLLATE utf8mb4_bin NOT NULL COMMENT '数据源名称',
  `variable_name` varchar(64) COLLATE utf8mb4_bin NOT NULL COMMENT '数据源变量名',
  `dblink_id` bigint(20) NOT NULL COMMENT '数据库链接Id',
  `master_table_id` bigint(20) NOT NULL COMMENT '主表Id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_user_id` bigint NOT NULL COMMENT '创建者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `update_user_id` bigint NOT NULL COMMENT '更新者',
  PRIMARY KEY (`datasource_id`),
  UNIQUE KEY `uk_app_code_variable_name` (`app_code`,`variable_name`) USING BTREE,
  KEY `idx_master_table_id` (`master_table_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='在线表单数据源表';

-- ----------------------------
-- 在线表单数据源关联表
-- ----------------------------
DROP TABLE IF EXISTS `zz_online_datasource_relation`;
CREATE TABLE `zz_online_datasource_relation` (
  `relation_id` bigint(20) NOT NULL COMMENT '主键Id',
  `app_code` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '应用编码',
  `relation_name` varchar(64) COLLATE utf8mb4_bin NOT NULL COMMENT '关联名称',
  `variable_name` varchar(128) COLLATE utf8mb4_bin NOT NULL COMMENT '变量名',
  `datasource_id` bigint(20) NOT NULL COMMENT '主数据源Id',
  `relation_type` int(11) NOT NULL COMMENT '关联类型',
  `master_column_id` bigint(20) NOT NULL COMMENT '主表关联字段Id',
  `slave_table_id` bigint(20) NOT NULL COMMENT '从表Id',
  `slave_column_id` bigint(20) NOT NULL COMMENT '从表关联字段Id',
  `cascade_delete` bit(1) NOT NULL COMMENT '删除主表的时候是否级联删除一对一和一对多的从表数据，多对多只是删除关联，不受到这个标记的影响。',
  `left_join` bit(1) NOT NULL COMMENT '是否左连接',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_user_id` bigint NOT NULL COMMENT '创建者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `update_user_id` bigint NOT NULL COMMENT '更新者',
  PRIMARY KEY (`relation_id`) USING BTREE,
  KEY `idx_app_code` (`app_code`) USING BTREE,
  UNIQUE KEY `uk_datasource_id_variable_name` (`datasource_id`,`variable_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='在线表单数据源关联表';

-- ----------------------------
-- 在线表单数据源和数据表关联的中间表
-- ----------------------------
DROP TABLE IF EXISTS `zz_online_datasource_table`;
CREATE TABLE `zz_online_datasource_table` (
  `id` bigint(20) NOT NULL COMMENT '主键Id',
  `datasource_id` bigint(20) NOT NULL COMMENT '数据源Id',
  `relation_id` bigint(20) DEFAULT NULL COMMENT '数据源关联Id',
  `table_id` bigint(20) NOT NULL COMMENT '数据表Id',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_relation_id` (`relation_id`) USING BTREE,
  KEY `idx_datasource_id` (`datasource_id`) USING BTREE,
  KEY `idx_table_id` (`table_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='在线表单数据源和数据表关联的中间表';

-- ----------------------------
-- 在线表单数据库链接表
-- ----------------------------
DROP TABLE IF EXISTS `zz_online_dblink`;
CREATE TABLE `zz_online_dblink` (
  `dblink_id` bigint(20) NOT NULL COMMENT '主键Id',
  `app_code` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '应用编码',
  `dblink_name` varchar(64) COLLATE utf8mb4_bin NOT NULL COMMENT '链接中文名称',
  `dblink_description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '链接描述',
  `dblink_type` int NOT NULL COMMENT '数据源类型',
  `configuration` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '配置信息',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_user_id` bigint NOT NULL COMMENT '创建者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `update_user_id` bigint NOT NULL COMMENT '更新者',
  PRIMARY KEY (`dblink_id`),
  KEY `idx_dblink_type` (`dblink_type`) USING BTREE,
  KEY `idx_app_code` (`app_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='在线表单数据库链接表';

-- ----------------------------
-- 在线表单字典表
-- ----------------------------
DROP TABLE IF EXISTS `zz_online_dict`;
CREATE TABLE `zz_online_dict` (
  `dict_id` bigint(20) NOT NULL COMMENT '主键Id',
  `app_code` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '应用编码',
  `dict_name` varchar(64) COLLATE utf8mb4_bin NOT NULL COMMENT '字典名称',
  `dict_type` int(11) NOT NULL COMMENT '字典类型',
  `dblink_id` bigint(20) DEFAULT NULL COMMENT '数据库链接Id',
  `table_name` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '字典表名称',
  `dict_code` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '全局字典编码',
  `key_column_name` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '字典表键字段名称',
  `parent_key_column_name` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '字典表父键字段名称',
  `value_column_name` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '字典值字段名称',
  `deleted_column_name` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '逻辑删除字段',
  `user_filter_column_name` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '用户过滤滤字段名称',
  `dept_filter_column_name` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '部门过滤滤字段名称',
  `tenant_filter_column_name` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '租户过滤字段名称',
  `tree_flag` bit(1) NOT NULL COMMENT '是否树形标记',
  `dict_list_url` varchar(512) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '获取字典列表数据的url',
  `dict_ids_url` varchar(512) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '根据主键id批量获取字典数据的url',
  `dict_data_json` text COLLATE utf8mb4_bin COMMENT '字典的JSON数据',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_user_id` bigint NOT NULL COMMENT '创建者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `update_user_id` bigint NOT NULL COMMENT '更新者',
  PRIMARY KEY (`dict_id`) USING BTREE,
  KEY `idx_app_code` (`app_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='在线表单字典表';

-- ----------------------------
-- 在线表单表单表
-- ----------------------------
DROP TABLE IF EXISTS `zz_online_form`;
CREATE TABLE `zz_online_form` (
  `form_id` bigint(20) NOT NULL COMMENT '主键Id',
  `tenant_id` bigint(20) COMMENT '租户id',
  `app_code` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '应用编码',
  `page_id` bigint(20) NOT NULL COMMENT '页面id',
  `form_code` varchar(128) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '表单编码',
  `form_name` varchar(64) COLLATE utf8mb4_bin NOT NULL COMMENT '表单名称',
  `form_kind` int(11) NOT NULL COMMENT '表单类别',
  `form_type` int(11) NOT NULL COMMENT '表单类型',
  `master_table_id` bigint(20) NOT NULL COMMENT '表单主表id',
  `widget_json` mediumtext COLLATE utf8mb4_bin COMMENT '表单组件JSON',
  `params_json` text COLLATE utf8mb4_bin COMMENT '表单参数JSON',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_user_id` bigint NOT NULL COMMENT '创建者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `update_user_id` bigint NOT NULL COMMENT '更新者',
  PRIMARY KEY (`form_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  UNIQUE KEY `uk_page_id_form_code` (`page_id`,`form_code`) USING BTREE,
  KEY `idx_app_code` (`app_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='在线表单表单表';

-- ----------------------------
-- 在线表单表单和数据源关联中间表
-- ----------------------------
DROP TABLE IF EXISTS `zz_online_form_datasource`;
CREATE TABLE `zz_online_form_datasource` (
  `id` bigint(20) NOT NULL COMMENT '主键Id',
  `form_id` bigint(20) NOT NULL COMMENT '表单Id',
  `datasource_id` bigint(20) NOT NULL COMMENT '数据源Id',
  PRIMARY KEY (`id`),
  KEY `idx_form_id` (`form_id`) USING BTREE,
  KEY `idx_datasource_id` (`datasource_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='在线表单表单和数据源关联中间表';

-- ----------------------------
-- 在线表单页面表
-- ----------------------------
DROP TABLE IF EXISTS `zz_online_page`;
CREATE TABLE `zz_online_page` (
  `page_id` bigint(20) NOT NULL COMMENT '主键Id',
  `tenant_id` bigint(20) COMMENT '租户id',
  `app_code` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '应用编码',
  `page_code` varchar(32) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '页面编码',
  `page_name` varchar(64) COLLATE utf8mb4_bin NOT NULL COMMENT '页面名称',
  `page_type` int(11) NOT NULL COMMENT '页面类型',
  `status` int(11) NOT NULL COMMENT '页面编辑状态',
  `published` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否发布',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_user_id` bigint NOT NULL COMMENT '创建者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `update_user_id` bigint NOT NULL COMMENT '更新者',
  PRIMARY KEY (`page_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_app_code` (`app_code`) USING BTREE,
  KEY `idx_page_code` (`page_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='在线表单页面表';

-- ----------------------------
-- 在线表单页面和数据源关联中间表
-- ----------------------------
DROP TABLE IF EXISTS `zz_online_page_datasource`;
CREATE TABLE `zz_online_page_datasource` (
  `id` bigint(20) NOT NULL COMMENT '主键Id',
  `page_id` bigint(20) NOT NULL COMMENT '页面主键Id',
  `datasource_id` bigint(20) NOT NULL COMMENT '数据源主键Id',
  PRIMARY KEY (`id`),
  KEY `idx_page_id` (`page_id`) USING BTREE,
  KEY `idx_datasource_id` (`datasource_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='在线表单页面和数据源关联中间表';

-- ----------------------------
-- 在线表单字段规则表
-- ----------------------------
DROP TABLE IF EXISTS `zz_online_rule`;
CREATE TABLE `zz_online_rule` (
  `rule_id` bigint(20) NOT NULL COMMENT '主键Id',
  `app_code` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '应用编码',
  `rule_name` varchar(64) COLLATE utf8mb4_bin NOT NULL COMMENT '规则名称',
  `rule_type` int(11) NOT NULL COMMENT '规则类型',
  `builtin` bit(1) NOT NULL COMMENT '内置规则标记',
  `pattern` varchar(512) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '自定义规则的正则表达式',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_user_id` bigint NOT NULL COMMENT '创建者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `update_user_id` bigint NOT NULL COMMENT '更新者',
  `deleted_flag` int(11) NOT NULL COMMENT '逻辑删除标记',
  PRIMARY KEY (`rule_id`) USING BTREE,
  KEY `idx_app_code` (`app_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='在线表单字段规则表';

INSERT INTO `zz_online_rule` VALUES (1,NULL,'只允许整数',1,b'1',NULL,CURDATE(),1689454707237457920,CURDATE(),1689454707237457920,1);
INSERT INTO `zz_online_rule` VALUES (2,NULL,'只允许数字',2,b'1',NULL,CURDATE(),1689454707237457920,CURDATE(),1689454707237457920,1);
INSERT INTO `zz_online_rule` VALUES (3,NULL,'只允许英文字符',3,b'1',NULL,CURDATE(),1689454707237457920,CURDATE(),1689454707237457920,1);
INSERT INTO `zz_online_rule` VALUES (4,NULL,'范围验证',4,b'1',NULL,CURDATE(),1689454707237457920,CURDATE(),1689454707237457920,1);
INSERT INTO `zz_online_rule` VALUES (5,NULL,'邮箱格式验证',5,b'1',NULL,CURDATE(),1689454707237457920,CURDATE(),1689454707237457920,1);
INSERT INTO `zz_online_rule` VALUES (6,NULL,'手机格式验证',6,b'1',NULL,CURDATE(),1689454707237457920,CURDATE(),1689454707237457920,1);

-- ----------------------------
-- 在线表单数据表
-- ----------------------------
DROP TABLE IF EXISTS `zz_online_table`;
CREATE TABLE `zz_online_table` (
  `table_id` bigint(20) NOT NULL COMMENT '主键Id',
  `app_code` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '应用编码',
  `table_name` varchar(64) COLLATE utf8mb4_bin NOT NULL COMMENT '表名称',
  `model_name` varchar(64) COLLATE utf8mb4_bin NOT NULL COMMENT '实体名称',
  `dblink_id` bigint(20) NOT NULL COMMENT '数据库链接Id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_user_id` bigint NOT NULL COMMENT '创建者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `update_user_id` bigint NOT NULL COMMENT '更新者',
  PRIMARY KEY (`table_id`),
  KEY `idx_dblink_id` (`dblink_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='在线表单数据表';

-- ----------------------------
-- 在线表单虚拟字段表
-- ----------------------------
DROP TABLE IF EXISTS `zz_online_virtual_column`;
CREATE TABLE `zz_online_virtual_column` (
  `virtual_column_id` bigint(20) NOT NULL COMMENT '主键Id',
  `table_id` bigint(20) NOT NULL COMMENT '所在表Id',
  `object_field_name` varchar(64) COLLATE utf8mb4_bin NOT NULL COMMENT '字段名称',
  `object_field_type` varchar(32) COLLATE utf8mb4_bin NOT NULL COMMENT '属性类型',
  `column_prompt` varchar(64) COLLATE utf8mb4_bin NOT NULL COMMENT '字段提示名',
  `virtual_type` int(11) NOT NULL COMMENT '虚拟字段类型(0: 聚合)',
  `datasource_id` bigint(20) NOT NULL COMMENT '关联数据源Id',
  `relation_id` bigint(20) DEFAULT NULL COMMENT '关联Id',
  `aggregation_table_id` bigint(20) DEFAULT NULL COMMENT '聚合字段所在关联表Id',
  `aggregation_column_id` bigint(20) DEFAULT NULL COMMENT '关联表聚合字段Id',
  `aggregation_type` int(11) DEFAULT NULL COMMENT '聚合类型(0: sum 1: count 2: avg 3: min 4: max)',
  `where_clause_json` varchar(1024) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '存储过滤条件的json',
  PRIMARY KEY (`virtual_column_id`) USING BTREE,
  KEY `idx_database_id` (`datasource_id`) USING BTREE,
  KEY `idx_relation_id` (`relation_id`) USING BTREE,
  KEY `idx_table_id` (`table_id`) USING BTREE,
  KEY `idx_aggregation_column_id` (`aggregation_column_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='在线表单虚拟字段表';

SET FOREIGN_KEY_CHECKS = 1;
