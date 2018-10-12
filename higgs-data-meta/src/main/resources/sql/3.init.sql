INSERT INTO agent_configuration (id, agent_id, configuration_name, configuration_key, configuration_value, configuration_unit, configuration_level, configuration_type, visible) VALUES (1, 0, '收集器ip', 'higgs.agent.collector.host', '10.200.10.43', null, 0, 0, 1);
INSERT INTO agent_configuration (id, agent_id, configuration_name, configuration_key, configuration_value, configuration_unit, configuration_level, configuration_type, visible) VALUES (2, 0, '收集器端口', 'higgs.agent.collector.port', '8099', null, 0, 0, 1);
INSERT INTO agent_configuration (id, agent_id, configuration_name, configuration_key, configuration_value, configuration_unit, configuration_level, configuration_type, visible) VALUES (3, 0, '采样率', 'higgs.sampling.rate', '100', '%', 0, 0, 1);
INSERT INTO agent_configuration (id, agent_id, configuration_name, configuration_key, configuration_value, configuration_unit, configuration_level, configuration_type, visible) VALUES (4, 0, '心跳周期', 'higgs.healthcheck.period', '60000', 'ms', 0, 0, 1);
INSERT INTO agent_configuration (id, agent_id, configuration_name, configuration_key, configuration_value, configuration_unit, configuration_level, configuration_type, visible) VALUES (5, 0, 'ApdexT', 'higgs.apdext.time', '100', 'ms', 0, 0, 0);

INSERT INTO agent_configuration (id, agent_id, configuration_name, configuration_key, configuration_value, configuration_unit, configuration_level, configuration_type, visible) VALUES (6, 0, '线程采样周期', 'higgs.thread.dump.period', '400', 'ms', 0, 1, 1);
INSERT INTO agent_configuration (id, agent_id, configuration_name, configuration_key, configuration_value, configuration_unit, configuration_level, configuration_type, visible) VALUES (7, 0, 'jvm采样周期', 'higgs.metrics.collect.period', '60000', 'ms', 0, 1, 1);
INSERT INTO agent_configuration (id, agent_id, configuration_name, configuration_key, configuration_value, configuration_unit, configuration_level, configuration_type, visible) VALUES (8, 0, '数据队列最大长度', 'higgs.data.buffer.size', '50000', 'ms', 0, 1, 1);
INSERT INTO agent_configuration (id, agent_id, configuration_name, configuration_key, configuration_value, configuration_unit, configuration_level, configuration_type, visible) VALUES (9, 0, '每次发送最大数量', 'higgs.data.send.batch.size', '1000', 'ms', 0, 1, 1);
INSERT INTO agent_configuration (id, agent_id, configuration_name, configuration_key, configuration_value, configuration_unit, configuration_level, configuration_type, visible) VALUES (10, 0, '发送频率', 'higgs.data.send.period', '10000', 'ms', 0, 1, 1);
INSERT INTO agent_configuration (id, agent_id, configuration_name, configuration_key, configuration_value, configuration_unit, configuration_level, configuration_type, visible) VALUES (11, 0, '忽略跟踪目标', 'higgs.trace.destination.ignore', '' , null, 0, 1, 1);
INSERT INTO agent_configuration (id, agent_id, configuration_name, configuration_key, configuration_value, configuration_unit, configuration_level, configuration_type, visible) VALUES (15, 0, 'servlet埋点白名单', 'higgs.profile.servlet', '', null, 0, 1, 1);

INSERT INTO agent_configuration (id, agent_id, configuration_name, configuration_key, configuration_value, configuration_unit, configuration_level, configuration_type, visible) VALUES (12, 0, '探针数据发送频率', 'higgs.browser.transFrequency', '1000', 'ms', 0, 3, 1);
INSERT INTO agent_configuration (id, agent_id, configuration_name, configuration_key, configuration_value, configuration_unit, configuration_level, configuration_type, visible) VALUES (13, 0, '是否启用浏览器端日志输出', 'higgs.browser.showlog', 'false', null, 0, 3, 1);
INSERT INTO agent_configuration (id, agent_id, configuration_name, configuration_key, configuration_value, configuration_unit, configuration_level, configuration_type, visible) VALUES (14, 0, '是否启用OpenTracing', 'higgs.browser.openTraceEnabled', 'false', null, 0, 3, 1);
INSERT INTO agent_configuration (id, agent_id, configuration_name, configuration_key, configuration_value, configuration_unit, configuration_level, configuration_type, visible) VALUES (16, 0, 'url白名单', 'higgs.http.excludeurl', '', null, 0, 0, 1);

INSERT INTO agent_configuration (id, agent_id, configuration_name, configuration_key, configuration_value, configuration_unit, configuration_level, configuration_type, visible) VALUES (17, 0, '是否追踪请求参数', 'higgs.http.reqparam.trace', 'true', null, 0, 1, 1);
INSERT INTO agent_configuration (id, agent_id, configuration_name, configuration_key, configuration_value, configuration_unit, configuration_level, configuration_type, visible) VALUES (18, 0, '单个请求参数长度限制', 'higgs.http.reqparam.eachlength', '64', null, 0, 1, 1);
INSERT INTO agent_configuration (id, agent_id, configuration_name, configuration_key, configuration_value, configuration_unit, configuration_level, configuration_type, visible) VALUES (19, 0, '所有请求参数长度限制', 'higgs.http.reqparam.totallength', '512', null, 0, 1, 1);
INSERT INTO agent_configuration (id, agent_id, configuration_name, configuration_key, configuration_value, configuration_unit, configuration_level, configuration_type, visible) VALUES (20, 0, '自定义业务方法监控', 'higgs.plugin.custom', '', null, 0, 3, 1);
