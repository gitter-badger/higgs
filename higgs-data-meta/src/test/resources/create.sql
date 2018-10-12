DROP DATABASE IF EXISTS higgs_management;
CREATE database higgs_management DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_general_ci;
USE higgs_management;

DROP TABLE IF EXISTS agent;
DROP TABLE IF EXISTS agent_configuration;
DROP TABLE IF EXISTS agent_threaddump;

CREATE TABLE `agent` (
  `id` bigint unsigned NOT NULL,
  `name` varchar(80) NOT NULL,
  `description` varchar(200),
  `type` tinyint(4) COMMENT '0 app , 1 tier, 2 instance',
  `app_id` bigint NOT NULL,
  `tier_id` bigint NOT NULL,
  `token` varchar(32) NOT NULL,
  `config_version` int(11) DEFAULT 0,
  `agent_fingerprint` varchar(120),
  `last_health_check_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `status` tinyint(4) DEFAULT '0' COMMENT '0 initialized, 1 active, 2 offline',
  `enabled` tinyint(4) DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uni_idx_token` (`token`),
  UNIQUE KEY `uni_idx_name` (`app_id`,`tier_id`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE `agent_configuration` (
  `id` bigint unsigned NOT NULL,
  `agent_id` bigint unsigned NOT NULL,
  `configuration_name` varchar(100) NOT NULL,
  `configuration_key` varchar(100) NOT NULL,
  `configuration_value` varchar(100),
  `configuration_unit` varchar(20),
  `configuration_level` tinyint(3) unsigned NOT NULL COMMENT '0 app , 1 tier, 2 instance',
  `configuration_type` tinyint(3) unsigned NOT NULL COMMENT '0 common , 1 java, 3 browser, 4 php',
  `visible` tinyint(4) NOT NULL DEFAULT '0' COMMENT '0 invisible , 1 visible',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `agent_threaddump` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `submit_time` datetime NOT NULL,
  `start_time` datetime,
  `app_id` bigint NOT NULL,
  `agent_id` bigint NOT NULL,
  `agent_token` varchar(20) NOT NULL,
  `dump_interval` int NOT NULL comment 'unit ms',
  `status` tinyint NOT NULL comment '0 prepare, 1 running, 2 complete, 3 cancel, 4 timeout after 15 min',
  `deliver_status` tinyint NOT NULL comment '0 no, 1 yes',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

