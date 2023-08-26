CREATE TABLE `seq_registry` (
  `seq_no` int(11) NOT NULL COMMENT '机器序号',
  `jvm_instance` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'JVM 示例 ip:pid',
  `renewal_time` datetime DEFAULT NULL COMMENT '续期时间',
  `version` bigint(10) NOT NULL DEFAULT '0' COMMENT '版本号',
  PRIMARY KEY (`seq_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='机器序号注册表\n机器序号注册表需要初始化10000条序号：0-9999';
