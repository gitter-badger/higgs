/*
 * Copyright 2018 The Higgs Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.vilada.higgs.processing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import io.vilada.higgs.processing.service.AgentService;
import io.vilada.higgs.processing.service.SpanService;
import io.vilada.higgs.serialization.thrift.factory.DefaultDeserializerFactory;
import io.vilada.higgs.serialization.thrift.factory.DefaultSerializerFactory;
import io.vilada.higgs.serialization.thrift.factory.DefaultSimpleJsonSerializerFactory;
import io.vilada.higgs.serialization.thrift.factory.ThreadLocalDeserializerFactory;
import io.vilada.higgs.serialization.thrift.factory.ThreadLocalSerializerFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TSerializer;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author mjolnir
 */
@Slf4j
public class HiggsJobContext {

    private static String CONFIG_XML_FILE = "classpath:higgs-job-service.xml";

    private static String JDBC_DATASOURCE = "dataSource";

    private static String JDBC_TEMPLATE = "jdbcTemplate";

    private static String JSON_OBJECT = "jsonObjectMapper";

    private static String CONFIG_OBJECT = "jobConfigProperties";

    private static HiggsJobContext INSTANCE = new HiggsJobContext();

    @Getter
    private JdbcTemplate jdbcTemplate;

    @Getter
    private HikariDataSource hikariDataSource;

    @Getter
    private SpanService spanService;

    @Getter
    private AgentService agentService;

    @Getter
    private ThreadLocalDeserializerFactory<TDeserializer> deserializerFactory;

    @Getter
    private ThreadLocalSerializerFactory<TSerializer> serializerFactory;

    @Getter
    private ThreadLocalSerializerFactory<TSerializer> simpleJsonSerializerFactory;

    @Getter
    private Map<String, String> esConfig;

    @Getter
    private List<InetSocketAddress> esTransportAddresses;

    @Getter
    private TransportClient esTransportClient;

    @Getter
    private int refinedSpanIncompleteSize = 10000;

    @Getter
    private long rawspanWindowSize = 15000;

    @Getter
    private long latencySpanWindowSize = 25000;

    @Getter
    private long highLatencySpanWindowSize = 120000;

    @Getter
    private long unusualLatencySpanWindowSize = 600000;

    @Getter
    private long rawSpanReprocessingWindexSize = 60000;

    @Getter
    private long refinedSpanAggregateWindowSize = 60000;

    @Getter
    private long agentStatProcessingWindowSize = 5000;

    @Getter
    private long agentInfoProcessingWindowSize = 3000;

    @Getter
    private long threadDumpProcessingWindowSize = 3000;

    @Getter
    private long webWindowSize = 10000;

    @Getter
    private int maxProcessTimes = 4;

    @Getter
    private ObjectMapper jsonObjectMapper;

    @Getter
    private long checkpointInterval = 25000L;

    @Getter
    private long checkpointTimeout = 60000L;

    @Getter
    private int jobParallelism = 1;

    @Getter
    private int rawspanParallelism = 16;

    @Getter
    private int latencyRawspanParallelism = 4;

    @Getter
    private int webParallelism = 4;

    @Getter
    private boolean enableCheckpoint = false;

    @Getter
    private Properties kafkaProperties;

    @Getter
    private String rawSpanTopic;

    @Getter
    private String rawSpanReprocessingTopic;

    @Getter
    private String latencyRawSpanTopic;

    @Getter
    private String highLantencyRawSpanTopic;

    @Getter
    private String unusualLantencyRawSpanTopic;

    @Getter
    private String refinedSpanAggregateTopic;

    @Getter
    private String agentInfoTopic;

    @Getter
    private String agentStatTopic;

    @Getter
    private String threadDumpTopic;

    @Getter
    private String webLoadTopic;

    @Getter
    private String webAjaxTopic;

    private HiggsJobContext() {
        try {
            ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(CONFIG_XML_FILE);
            hikariDataSource = applicationContext.getBean(JDBC_DATASOURCE, HikariDataSource.class);
            jdbcTemplate = applicationContext.getBean(JDBC_TEMPLATE, JdbcTemplate.class);
            jsonObjectMapper = applicationContext.getBean(JSON_OBJECT, ObjectMapper.class);
            spanService = applicationContext.getBean(SpanService.class);
            agentService = applicationContext.getBean(AgentService.class);
            deserializerFactory = new ThreadLocalDeserializerFactory<>(DefaultDeserializerFactory.INSTANCE);
            serializerFactory = new ThreadLocalSerializerFactory<>(DefaultSerializerFactory.INSTANCE);
            simpleJsonSerializerFactory = new ThreadLocalSerializerFactory<>(
                    DefaultSimpleJsonSerializerFactory.INSTANCE);

            Properties configProperties = applicationContext.getBean(CONFIG_OBJECT, Properties.class);
            initFlinkJobConfig(configProperties);
            initKafkaConfig(configProperties);
            initElasticSearch(configProperties);

        } catch (Exception e) {
            log.error("job HiggsJobContext init failed.", e);
        }
    }

    public static HiggsJobContext getInstance() {
        return INSTANCE;
    }

    public static void shutdown() {
        try {
            if (INSTANCE.hikariDataSource != null) {
                INSTANCE.hikariDataSource.close();
                log.info("close hikariDataSource succeeded.");
            }
        } catch (Exception e) {
            log.error("close hikariDataSource failed", e);
        }
        try {
            if (INSTANCE.esTransportClient != null) {
                INSTANCE.esTransportClient.close();
                log.info("close elasticsearch transportClient succeeded.");
            }
        } catch (Exception e) {
            log.error("elasticsearch transportClient close failed", e);
        }
        try {
            if (INSTANCE.spanService != null) {
                INSTANCE.spanService.destroySpanService();
                log.info("destroy SpanService succeeded.");
            }
        } catch (Exception e) {
            log.error("destroy SpanService failed", e);
        }
    }

    private void initFlinkJobConfig(Properties configProperties) {
        enableCheckpoint = Boolean.parseBoolean(configProperties.getProperty(
                FlinkJobConstants.JOB_CHECKPOINT_ENABLE_FIELD, "false"));
        jobParallelism = Integer.parseInt(configProperties.getProperty(
                FlinkJobConstants.JOB_DEFAULT_PARALLELISM_FIELD, Integer.toString(jobParallelism)));

        rawspanParallelism = Integer.parseInt(configProperties.getProperty(
                FlinkJobConstants.JOB_RAWSPAN_PARALLELISM_FIELD, Integer.toString(rawspanParallelism)));

        latencyRawspanParallelism = Integer.parseInt(configProperties.getProperty(
                FlinkJobConstants.JOB_RAWSPAN_LATENCY_PARALLELISM_FIELD, Integer.toString(latencyRawspanParallelism)));

        webParallelism = Integer.parseInt(configProperties.getProperty(
                FlinkJobConstants.JOB_WEB_PARALLELISM_FIELD, Integer.toString(webParallelism)));

        checkpointInterval = Long.parseLong(configProperties.getProperty(
                FlinkJobConstants.JOB_CHECKPOINT_INTERVAL_FIELD, Long.toString(checkpointInterval)));
        checkpointTimeout = Long.parseLong(configProperties.getProperty(
                FlinkJobConstants.JOB_CHECKPOINT_TIMEOUT_FIELD, Long.toString(checkpointTimeout)));

        rawspanWindowSize = Long.parseLong(configProperties.getProperty(
                FlinkJobConstants.JOB_RAWSPAN_WINDOW_SIZE_FIELD, Long.toString(rawspanWindowSize)));

        latencySpanWindowSize = Long.parseLong(configProperties.getProperty(
                FlinkJobConstants.JOB_LATENCY_RAWSPAN_WINDOW_SIZE_FIELD, Long.toString(latencySpanWindowSize)));

        highLatencySpanWindowSize = Long.parseLong(configProperties.getProperty(
                FlinkJobConstants.JOB_HIGH_LATENCY_RAWSPAN_WINDOW_SIZE_FIELD, Long.toString(highLatencySpanWindowSize)));

        unusualLatencySpanWindowSize = Long.parseLong(configProperties.getProperty(
                FlinkJobConstants.JOB_UNUSUAL_LATENCY_RAWSPAN_WINDOW_SIZE_FIELD, Long.toString(unusualLatencySpanWindowSize)));

        rawSpanReprocessingWindexSize = Long.parseLong(configProperties.getProperty(
                FlinkJobConstants.JOB_REPROCESSING_RAWSPAN__WINDOW_SIZE_FIELD, Long.toString(rawSpanReprocessingWindexSize)));

        refinedSpanAggregateWindowSize = Long.parseLong(configProperties.getProperty(FlinkJobConstants.JOB_REFINEDSPAN_AGGREGATE_WINDOW_SIZE_FIELD,
                    Long.toString(refinedSpanAggregateWindowSize)));

        agentStatProcessingWindowSize = Long.parseLong(configProperties.getProperty(FlinkJobConstants.JOB_ROCESSING_AGENTSTAT_WINDOW_SIZE_FIELD,
                Long.toString(agentStatProcessingWindowSize)));

        agentInfoProcessingWindowSize = Long.parseLong(configProperties.getProperty(FlinkJobConstants.JOB_PROCESSING_AGENTINFO_WINDOW_SIZE_FIELD,
                Long.toString(agentInfoProcessingWindowSize)));

        threadDumpProcessingWindowSize = Long.parseLong(configProperties.getProperty(FlinkJobConstants.JOB_PROCESSING_THREADDUMP_WINDOW_SIZE_FIELD,
                Long.toString(threadDumpProcessingWindowSize)));

        webWindowSize = Long.parseLong(configProperties.getProperty(FlinkJobConstants.JOB_PROCESSING_WEB_WINDOW_SIZE_FIELD,
                Long.toString(webWindowSize)));

        maxProcessTimes = Integer.parseInt(configProperties.getProperty(FlinkJobConstants.JOB_REFINEDSPAN_MAX_PROCESS_TIMES_FIELD,
                Long.toString(maxProcessTimes)));
    }

    private void initKafkaConfig(Properties configProperties) {
        kafkaProperties = new Properties();
        kafkaProperties.setProperty(FlinkJobConstants.KAFKA_BROKER_SERVER_CONFIG_FIELD,
                configProperties.getProperty(FlinkJobConstants.JOB_KAFKA_SERVER_FIELD));
        kafkaProperties.setProperty(FlinkJobConstants.KAFKA_CONSUMER_GROUP_CONFIG_FIELD,
                configProperties.getProperty(FlinkJobConstants.JOB_KAFKA_GROUP_FIELD));

        rawSpanTopic = configProperties.getProperty(FlinkJobConstants.JOB_KAFKA_RAWSPAN_TOPIC_FIELD);
        rawSpanReprocessingTopic = configProperties.getProperty(FlinkJobConstants.JOB_KAFKA_RAWSPAN_REPROCESSING_TOPIC_FIELD);
        latencyRawSpanTopic = configProperties.getProperty(FlinkJobConstants.JOB_KAFKA_RAWSPAN_LATENCY_TOPIC_FIELD);
        highLantencyRawSpanTopic = configProperties.getProperty(FlinkJobConstants.JOB_KAFKA_RAWSPAN_HIGH_LATENCY_TOPIC_FIELD);
        unusualLantencyRawSpanTopic = configProperties.getProperty(
                FlinkJobConstants.JOB_KAFKA_RAWSPAN_UNUSUAL_LATENCY_TOPIC_FIELD);
        refinedSpanAggregateTopic = configProperties.getProperty(FlinkJobConstants.JOB_KAFKA_REFINEDSPAN_AGGREGATE_TOPIC_FIELD);

        agentInfoTopic = configProperties.getProperty(FlinkJobConstants.JOB_KAFKA_AGENT_INFO_TOPIC_FIELD);
        agentStatTopic = configProperties.getProperty(FlinkJobConstants.JOB_KAFKA_AGENT_STAT_TOPIC_FIELD);
        threadDumpTopic = configProperties.getProperty(FlinkJobConstants.JOB_KAFKA_THEAD_DUMP_TOPIC_FIELD);

        webLoadTopic = configProperties.getProperty(FlinkJobConstants.JOB_KAFKA_WEB_LOAD_TOPIC_FIELD);
        webAjaxTopic = configProperties.getProperty(FlinkJobConstants.JOB_KAFKA_WEB_AJAX_TOPIC_FIELD);
    }

    private void initElasticSearch(Properties configProperties) throws JobInitException {

        try {
            refinedSpanIncompleteSize = Integer.parseInt(configProperties.getProperty(
                    FlinkJobConstants.ES_REFINEDSPAN_INCOMPLETE_SIZE_FIELD, Integer.toString(refinedSpanIncompleteSize)));

            esConfig = new HashMap<>();
            String elasticsearchClusterName = configProperties.getProperty(
                    FlinkJobConstants.JOB_ES_CLUSTER_NODE_NAME_FIELD);
            esConfig.put(FlinkJobConstants.ES_CLUSTER_NAME_CONFIG_FIELD, elasticsearchClusterName);
            esConfig.put(FlinkJobConstants.ES_BULK_SIZE_CONFIG_FIELD, configProperties.getProperty(
                    FlinkJobConstants.JOB_ES_BULK_SIZE_FIELD));
            esConfig.put(FlinkJobConstants.ES_INTERVAL_MS_CONFIG_FIELD, configProperties.getProperty(
                    FlinkJobConstants.JOB_ES_FLUSH_INTERVAL_FIELD));

            String esNodes = configProperties.getProperty(
                    FlinkJobConstants.JOB_ES_CLUSTER_NODE_FIELD);
            if (!StringUtils.hasText(esNodes)){
                throw new JobInitException("es node config invalid.");
            }
            String[] esNodeArray = esNodes.split(FlinkJobConstants.COMMA_DELIMITER);
            if (esNodeArray == null || esNodeArray.length < 1) {
                throw new JobInitException("es node config was not found.");
            }

            Settings settings = Settings.settingsBuilder().put(FlinkJobConstants.ES_CLUSTER_NAME_CONFIG_FIELD,
                    elasticsearchClusterName).build();
            esTransportClient = TransportClient.builder().settings(settings).build();

            esTransportAddresses = new ArrayList<>();
            for (String esNode : esNodeArray) {
                if (StringUtils.hasText(esNode)) {
                    String[] addressArray = esNode.split(FlinkJobConstants.SEMICOLON_DELIMITER);
                    if (addressArray == null || addressArray.length < 2) {
                        continue;
                    }
                    InetSocketAddress inetSocketAddress =
                            new InetSocketAddress(addressArray[0], Integer.parseInt(addressArray[1]));
                    esTransportAddresses.add(inetSocketAddress);

                    esTransportClient.addTransportAddress(new InetSocketTransportAddress(
                            InetAddress.getByName(inetSocketAddress.getHostName()), inetSocketAddress.getPort()));
                }
            }
        } catch (UnknownHostException e) {
            throw new JobInitException("init elastic search failed.", e);
        }
    }
}
