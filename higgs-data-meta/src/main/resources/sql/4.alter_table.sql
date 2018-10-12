-- 新增判断删除的字段
ALTER TABLE agent
  ADD is_deleted TINYINT DEFAULT 0 NULL;

-- 更新‰为%
UPDATE agent_configuration
SET configuration_value = '100', configuration_unit = '%'
WHERE id = 3 AND configuration_key = 'higgs.sampling.rate';

-- 更新字段长度, 义明的一个配置需要较多空间
ALTER TABLE agent_configuration
  MODIFY configuration_value VARCHAR(2000) NOT NULL;

-- APM-1831 , 为/agent/listInstanceByTierId这个接口需要添加一个字段，即原始主机名信息
ALTER TABLE agent
  ADD original_name VARCHAR(80) NULL;

-- 更新默认ApdexT的显示模式为隐藏
UPDATE agent_configuration
SET visible = 0
WHERE id = 5 AND configuration_key = 'higgs.apdext.time';

-- 更新agent的status的注释
ALTER TABLE agent
  MODIFY status TINYINT(4) DEFAULT '0'
  COMMENT '0 offline , 1 online'
