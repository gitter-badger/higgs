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

package io.vilada.higgs.processing.function.span;

import io.vilada.higgs.data.common.document.RefinedSpan;
import io.vilada.higgs.data.common.document.RefinedSpanExtraContext;
import io.vilada.higgs.processing.FlinkJobConstants;
import io.vilada.higgs.processing.HiggsJobContext;
import io.vilada.higgs.processing.dto.AggregateInstance;
import io.vilada.higgs.processing.service.SpanService;
import io.vilada.higgs.serialization.thrift.factory.ThreadLocalSerializerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.streaming.connectors.elasticsearch.ElasticsearchSinkFunction;
import org.apache.flink.streaming.connectors.elasticsearch.RequestIndexer;
import org.apache.thrift.TSerializer;
import org.elasticsearch.action.index.IndexRequest;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static io.vilada.higgs.data.common.constant.ESIndexConstants.REFINEDSPAN_INDEX;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.REFINEDSPAN_TYPE;

/**
 * @author ethan
 */
@Slf4j
public class RefinedSpanElasticsearchSinkFunction implements ElasticsearchSinkFunction<Iterable<RefinedSpan>> {

    private static DateTimeFormatter MINUTE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd HHmm")
                        .withZone(ZoneOffset.UTC);

    @Override
    public void process(Iterable<RefinedSpan> refinedSpanList, RuntimeContext ctx, RequestIndexer indexer) {
        HiggsJobContext higgsJobContext = HiggsJobContext.getInstance();
        Map<String, String> aggregateInstanceMap = new HashMap<>();
        ObjectMapper jsonObjectMapper = higgsJobContext.getJsonObjectMapper();
        ThreadLocalSerializerFactory<TSerializer> serializerFactory = higgsJobContext.getSerializerFactory();
        SpanService spanService = higgsJobContext.getSpanService();
        for (RefinedSpan refinedSpan : refinedSpanList) {
            try {
                RefinedSpanExtraContext spanExtraContext = refinedSpan.getExtraContext();
                spanExtraContext.setTransformed(null);
                indexer.add(new IndexRequest(REFINEDSPAN_INDEX, REFINEDSPAN_TYPE,
                        refinedSpan.getId()).source(jsonObjectMapper.writeValueAsBytes(refinedSpan)));
                notifyAggregation(refinedSpan, aggregateInstanceMap, spanService, serializerFactory);
            } catch (Exception e) {
                log.error("RefinedSpanElasticsearchSinkFunction process failed.", e);
            }
        }
    }

    private void notifyAggregation(RefinedSpan refinedSpan, Map<String, String> aggregateInstanceMap,
            SpanService spanService, ThreadLocalSerializerFactory<TSerializer> serializerFactory) throws Exception {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(refinedSpan.getFinishTime()), ZoneOffset.UTC);
        String zonedDateTimeStr = zonedDateTime.format(MINUTE_FORMAT);
        ZonedDateTime zonedDateTimeWithNoSeconds = ZonedDateTime.parse(zonedDateTimeStr, MINUTE_FORMAT);
        Long epochMilli = Long.valueOf(zonedDateTimeWithNoSeconds.toInstant().toEpochMilli());
        String instanceId = refinedSpan.getContext().getInstanceId();
        String key = new StringBuilder().append(instanceId).append(FlinkJobConstants.COMMA_DELIMITER)
                             .append(epochMilli.toString()).toString();
        if (aggregateInstanceMap.get(key) == null) {
            spanService.notifyAggregateInstance(serializerFactory.createSerializer().serialize(
                    new AggregateInstance(refinedSpan.getContext().getInstanceId(), epochMilli)));
            aggregateInstanceMap.put(key, "");
        }
    }
}
