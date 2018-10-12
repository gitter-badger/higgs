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

package io.vilada.higgs.processing.service;

import io.vilada.higgs.data.common.document.RefinedSpan;
import io.vilada.higgs.processing.FlinkJobConstants;
import io.vilada.higgs.processing.HiggsJobContext;
import io.vilada.higgs.processing.TraceStatueEnum;
import io.vilada.higgs.processing.bo.AgentBO;
import io.vilada.higgs.processing.bo.RefinedSpanAggregationBatch;
import io.vilada.higgs.processing.dto.ReprocessingTrace;
import io.vilada.higgs.processing.service.aggregation.DatabaseAggregationService;
import io.vilada.higgs.processing.service.aggregation.ErrorAggregationService;
import io.vilada.higgs.processing.service.aggregation.RemoteCallAggregationService;
import io.vilada.higgs.processing.service.aggregation.TransactionAggregationService;
import io.vilada.higgs.serialization.thrift.factory.ThreadLocalSerializerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.thrift.TSerializer;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static io.vilada.higgs.data.common.constant.ESIndexConstants.*;
import static io.vilada.higgs.processing.FlinkJobConstants.*;

/**
 * @author ethan
 */
@Slf4j
@Service
public class SpanService {

    @Autowired
    private AgentService agentService;

    @Autowired
    private ObjectMapper jsonObjectMapper;

    @Resource(name = "jobConfigProperties")
    private Properties jobConfigProperties;

    @Autowired
    TransactionAggregationService transactionAggregationService;

    @Autowired
    RemoteCallAggregationService remoteCallAggregationService;

    @Autowired
    DatabaseAggregationService databaseAggregationService;

    @Autowired
    ErrorAggregationService errorAggregationService;

    private LoadingCache<String, AgentBO> agentCache;

    private Producer<byte[], byte[]> producer;

    private String refinedSpanAggregateTopic;

    private String rawSpanReprocessingTopic;

    @PostConstruct
    private void initSpanService() {
        agentCache = CacheBuilder.newBuilder()
             .maximumSize(1000)
             .expireAfterWrite(10, TimeUnit.MINUTES)
             .build(new CacheLoader<String, AgentBO>() {
                 @Override
                 public AgentBO load(String key) throws Exception {
                     return agentService.getAgentByToken(key);
                 }
             });

        refinedSpanAggregateTopic = jobConfigProperties.getProperty(JOB_KAFKA_REFINEDSPAN_AGGREGATE_TOPIC_FIELD);
        rawSpanReprocessingTopic = jobConfigProperties.getProperty(JOB_KAFKA_RAWSPAN_REPROCESSING_TOPIC_FIELD);

        Properties props = new Properties();
        props.put(KAFKA_BROKER_SERVER_CONFIG_FIELD,
                jobConfigProperties.getProperty(JOB_KAFKA_SERVER_FIELD));
        props.put(KAFKA_KEY_SERIALIZER_FIELD,
                jobConfigProperties.getProperty(JOB_KAFKA_SERIALIZER_FIELD));
        props.put(KAFKA_VALUE_SERIALIZER_FIELD,
                jobConfigProperties.getProperty(JOB_KAFKA_SERIALIZER_FIELD));
        props.put(KAFKA_COMPRESSION_FIELD, jobConfigProperties.getProperty(
                JOB_KAFKA_COMPRESSION_FIELD, KAFKA_DEFAULT_COMPRESSION_TYPE));
        producer = new KafkaProducer<>(props);
    }

    public RefinedSpan transformRawSpan(RefinedSpan refinedSpan) {
        try {
            if (refinedSpan == null) {
                return null;
            }
            if (refinedSpan.getExtraContext() != null &&
                        refinedSpan.getExtraContext().getTransformed() != null) {
                return refinedSpan;
            }
            if(refinedSpan.getSpanTags() == null || refinedSpan.getContext() == null){
                log.warn("SpanTags is null in span:{}", refinedSpan);
                return null;
            }
            TraceTransformer.transform(refinedSpan,
                    getAgentByToken(refinedSpan.getContext().getParentAgentToken()));
            return refinedSpan;
        } catch (Exception e) {
            log.error("invalid raw span data.", e);
        }
        return null;
    }

    public List<RefinedSpan> searchIncompleteSpanInES(String index, String type, String traceId) {
        try {
            HiggsJobContext higgsJobContext = HiggsJobContext.getInstance();
            TransportClient transportClient = higgsJobContext.getEsTransportClient();
            SearchResponse searchResponse = transportClient.prepareSearch(index).setTypes(type)
                    .setQuery(QueryBuilders.boolQuery().filter(
                            QueryBuilders.termQuery(FlinkJobConstants.CONTEXT_TRACE_ID, traceId)))
                    .setFrom(FlinkJobConstants.INCOMPLETE_FROM)
                    .setSize(higgsJobContext.getRefinedSpanIncompleteSize())
                    .execute().actionGet();
            SearchHits searchHits = searchResponse.getHits();
            if (searchHits.getTotalHits() < 1) {
                return null;
            }
            SearchHit[] searchHitsArray = searchHits.getHits();
            List<RefinedSpan> incompleteSpanList = new ArrayList<>(searchHitsArray.length);
            for (SearchHit searchHit : searchHitsArray) {
                if (searchHit.isSourceEmpty()) {
                    continue;
                }
                RefinedSpan refinedSpan = jsonObjectMapper.readValue(searchHit.source(), RefinedSpan.class);
                refinedSpan.setId(searchHit.getId());
                incompleteSpanList.add(refinedSpan);
            }
            return incompleteSpanList;
        } catch (Exception e) {
            log.error("searchIncompleteSpanInES failed.", e);
        }
        return null;
    }

    public List<RefinedSpan> scrollIncompleteSpanInES(String index, String type, String traceId) {
        TransportClient transportClient = HiggsJobContext.getInstance().getEsTransportClient();
        SearchResponse scrollResp = null;
        try {
            scrollResp = transportClient.prepareSearch(index).setTypes(type)
                    .setScroll(new TimeValue(60000))
                    .setQuery(QueryBuilders.boolQuery().filter(
                                    QueryBuilders.termQuery(FlinkJobConstants.CONTEXT_TRACE_ID, traceId)))
                    .setSize(5000).execute().actionGet();
            List<RefinedSpan> incompleteSpanList = new ArrayList<>(5000);
            while (true) {
                for (SearchHit searchHit : scrollResp.getHits().getHits()) {
                    if (searchHit.isSourceEmpty()) {
                        continue;
                    }
                    RefinedSpan refinedSpan = jsonObjectMapper.readValue(
                            searchHit.source(), RefinedSpan.class);
                    refinedSpan.setId(searchHit.getId());
                    incompleteSpanList.add(refinedSpan);
                }
                scrollResp = transportClient.prepareSearchScroll(scrollResp.getScrollId())
                                     .setScroll(new TimeValue(60000)).execute().actionGet();
                if (scrollResp.getHits().getHits().length == 0) {
                    break;
                }
            }
            return incompleteSpanList;
        } catch (Exception e) {
            log.error("scrollIncompleteSpanInES failed", e);
        } finally {
            if (scrollResp != null) {
                transportClient.prepareClearScroll().addScrollId(scrollResp.getScrollId())
                        .execute().actionGet();
            }
        }
        return null;
    }

    public void dispatchIncompleteRawSpan(TraceStatueEnum traceStatueEnum,
        String dispatchTopic, Iterable<RefinedSpan> refinedSpanIterable) {
        HiggsJobContext higgsJobContext = HiggsJobContext.getInstance();
        ThreadLocalSerializerFactory<TSerializer> serializerFactory = higgsJobContext.getSerializerFactory();
        if (TraceStatueEnum.INCOMPLETE == traceStatueEnum) {
            for (RefinedSpan refinedSpan : refinedSpanIterable) {
                try {
                    producer.send(new ProducerRecord<>(dispatchTopic,
                            Long.toString(System.currentTimeMillis()).getBytes(Charsets.UTF_8),
                            serializerFactory.createSerializer().serialize(refinedSpan)));
                } catch (Exception e) {
                    log.error("dispatchLatencyRawSpan failed.", e);
                }
            }
        } else if (TraceStatueEnum.OVERHEAD == traceStatueEnum) {
            saveToElasticSearch(OVERHEAD_REFINEDSPAN_INDEX,
                    OVERHEAD_REFINEDSPAN_TYPE, refinedSpanIterable);
        }
    }

    public void dispatchIncompleteRawSpanForSpeedJob(boolean isSaveToTempIndex, String traceId, int processedTimes,
            TraceStatueEnum traceStatueEnum, Iterable<RefinedSpan> refinedSpanIterable) {
        try {
            if (TraceStatueEnum.INCOMPLETE == traceStatueEnum) {
                if (isSaveToTempIndex) {
                    saveToElasticSearch(TEMP_REFINEDSPAN_INDEX,
                            REFINEDSPAN_TYPE, refinedSpanIterable);
                }
                producer.send(new ProducerRecord<>(rawSpanReprocessingTopic,
                        Long.toString(System.currentTimeMillis()).getBytes(Charsets.UTF_8),
                        HiggsJobContext.getInstance().getSerializerFactory().createSerializer()
                                .serialize(new ReprocessingTrace(traceId, processedTimes))));
            } else if (TraceStatueEnum.OVERHEAD == traceStatueEnum) {
                saveToElasticSearch(OVERHEAD_REFINEDSPAN_INDEX,
                        OVERHEAD_REFINEDSPAN_TYPE, refinedSpanIterable);
            }
        } catch (Exception e) {
            log.error("dispatchIncompleteRawSpanForSpeedJob failed", e);
        }
    }

    public void saveToElasticSearch(String indexName, String type,
            Iterable<RefinedSpan> refinedSpanIterable) {
        try {
            HiggsJobContext higgsJobContext = HiggsJobContext.getInstance();
            ThreadLocalSerializerFactory<TSerializer> simpleJsonSerializerFactory =
                    higgsJobContext.getSimpleJsonSerializerFactory();
            TransportClient transportClient = higgsJobContext.getEsTransportClient();
            BulkRequestBuilder bulkRequest = transportClient.prepareBulk();
            for (RefinedSpan refinedSpan : refinedSpanIterable) {
                try {
                    byte[] refinedSpanByteArray = simpleJsonSerializerFactory
                                .createSerializer().serialize(refinedSpan);
                    IndexRequestBuilder indexRequestBuilder = transportClient.prepareIndex(
                            indexName, type, refinedSpan.getId()).setSource(refinedSpanByteArray);
                    bulkRequest.add(indexRequestBuilder);
                } catch (Exception e) {
                    log.error("dispatch one latency rawSpan failed.", e);
                }
            }
            BulkResponse bulkResponse = bulkRequest.get();
            if (bulkResponse.hasFailures()) {
                log.error("save fucking spans failed.");
            }
        } catch (Exception e) {
            log.error("dispatchLatencyRawSpan failed.", e);
        }
    }

    public void notifyAggregateInstance(byte[] aggregateInstanceBytes) {
        try {
            producer.send(new ProducerRecord<>(refinedSpanAggregateTopic,
                    Long.toString(System.currentTimeMillis()).getBytes(Charsets.UTF_8), aggregateInstanceBytes));
        } catch (Exception e) {
            log.error("send aggregateInstance to refinedSpanAggregateTopic failed.", e);
        }
    }

    public void executeMinuteAggregation(RefinedSpanAggregationBatch refinedSpanAggregationBatch) {
        transactionAggregationService.aggregate(refinedSpanAggregationBatch);
        remoteCallAggregationService.aggregate(refinedSpanAggregationBatch);
        databaseAggregationService.aggregate(refinedSpanAggregationBatch);
        errorAggregationService.aggregate(refinedSpanAggregationBatch);
    }

    private AgentBO getAgentByToken(String token) {
        if (token == null) {
            return null;
        }
        AgentBO agentBO;
        try {
            agentBO = agentCache.get(token);
            if(agentBO == null || agentBO.getId() == null){
                log.warn("agent was not found, agent token {}", token);
                return null;
            }
        } catch (ExecutionException e) {
            log.error("agent load from cache failed, agent token {}", token);
            return null;
        }
        return agentBO;
    }

    @PreDestroy
    public void destroySpanService() {
        if (producer != null) {
            producer.close();
        }
    }
}
