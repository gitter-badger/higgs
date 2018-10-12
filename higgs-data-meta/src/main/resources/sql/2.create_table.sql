DROP TABLE IF EXISTS agent;
DROP TABLE IF EXISTS agent_configuration;
DROP TABLE IF EXISTS agent_threaddump;

create table agent
(
  id bigint not null PRIMARY KEY,
  name varchar(80) not null,
  original_name VARCHAR(80) null
  description varchar(200) null,
  type tinyint null comment '1 Java, 3 Browser, 4 PHP',
  app_id bigint not null,
  tier_id bigint not null,
  token varchar(32) not null,
  config_version int default '0' null,
  agent_fingerprint varchar(120) null,
  last_health_check_time datetime DEFAULT CURRENT_TIMESTAMP,
  status tinyint default '0' null comment '0 offline , 1 online',
  enabled tinyint default '1' null,
  is_visible tinyint default '1' null comment 'visible/count in some specific condition',
  is_deleted tinyint DEFAULT '0' null comment 'is current object deleted, 0 not deleted, 1 deleted',
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE INDEX uni_idx_token (token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

create table agent_configuration
(
  id bigint not null PRIMARY KEY,
  agent_id bigint not null,
  configuration_name varchar(100) not null,
  configuration_key varchar(100) not null,
  configuration_value varchar(2000) not null,
  configuration_unit varchar(20) null,
  configuration_level tinyint(3) unsigned not null comment '0 default , 1 app , 2 tier, 3 instance',
  configuration_type tinyint(3) unsigned not null comment '0 common , 1 java, 3 browser, 4 php',
  visible tinyint default '0' not null comment '0 invisible , 1 visible',
  INDEX idx_agent_id (agent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

create table agent_threaddump
(
  id bigint auto_increment PRIMARY KEY,
  submit_time datetime not null,
  start_time datetime null,
  app_id bigint not null,
  agent_id bigint not null,
  agent_token varchar(20) not null,
  dump_interval int not null comment 'unit ms',
  status tinyint not null comment '0 prepare, 1 running, 2 complete, 3 cancel, 4 timeout after 15 min',
  deliver_status tinyint not null comment '0 no, 1 yes',
  INDEX idx_agent_token (agent_token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
