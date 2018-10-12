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

package io.vilada.higgs.processing.test;

import io.vilada.higgs.data.common.document.RefinedSpan;
import io.vilada.higgs.data.common.document.RefinedSpanExtraContext;
import io.vilada.higgs.processing.FlinkJobConstants;
import io.vilada.higgs.processing.service.TraceComputeAssistor;
import io.vilada.higgs.serialization.thrift.dto.TSpan;
import io.vilada.higgs.serialization.thrift.dto.TSpanContext;
import io.vilada.higgs.serialization.thrift.factory.DefaultDeserializerFactory;
import io.vilada.higgs.serialization.thrift.factory.DefaultSerializerFactory;
import io.vilada.higgs.serialization.thrift.factory.DefaultSimpleJsonSerializerFactory;
import io.vilada.higgs.serialization.thrift.factory.ThreadLocalDeserializerFactory;
import io.vilada.higgs.serialization.thrift.factory.ThreadLocalSerializerFactory;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TSerializer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ethan
 */

public class TraceComputeAssistorTests {

    Iterable<RefinedSpan> refinedSpanIterable;

    List<RefinedSpan> oneSpan;

    String agent1TransactionName = "/uri";

    String agent2TransactionName = "/uri2";

    @Before
    public void initSpanData() {
        String traceId = "0";
        List<RefinedSpan> refinedSpanList = new ArrayList<>();
        RefinedSpan refinedSpan = new RefinedSpan();
        TSpanContext context = new TSpanContext();
        refinedSpan.setContext(context);
        RefinedSpanExtraContext extraContext = new RefinedSpanExtraContext();
        refinedSpan.setExtraContext(extraContext);
        context.setAppId("1");
        context.setTierId("1");
        context.setInstanceId("1");
        context.setTraceId(traceId);
        context.setSpanId("1");
        context.setParentSpanId("-1");

        extraContext.setSpanTransactionName(agent1TransactionName);
        extraContext.setElapsed(10);
        Map<String, String> spanTags = new HashMap<>();
        spanTags.put(FlinkJobConstants.REFINED_SPAN_COMPONENT_DESTINATION, "HTTP");
        refinedSpan.setSpanTags(spanTags);
        refinedSpanList.add(refinedSpan);
        refinedSpan = new RefinedSpan();
        context = new TSpanContext();
        refinedSpan.setContext(context);
        extraContext = new RefinedSpanExtraContext();
        refinedSpan.setExtraContext(extraContext);
        context.setAppId("1");
        context.setTierId("1");
        context.setInstanceId("1");
        context.setTraceId(traceId);
        context.setSpanId("2");
        context.setParentSpanId("1");
        extraContext.setElapsed(8);
        spanTags = new HashMap<>();
        refinedSpan.setSpanTags(spanTags);
        refinedSpanList.add(refinedSpan);

        refinedSpan = new RefinedSpan();
        context = new TSpanContext();
        refinedSpan.setContext(context);
        extraContext = new RefinedSpanExtraContext();
        refinedSpan.setExtraContext(extraContext);
        context.setAppId("1");
        context.setTierId("1");
        context.setInstanceId("1");
        context.setTraceId(traceId);
        context.setSpanId("3");
        context.setParentSpanId("2");
        extraContext.setElapsed(7);
        spanTags = new HashMap<>();
        spanTags.put(FlinkJobConstants.REFINED_SPAN_COMPONENT_TARGET, "HTTP");
        refinedSpan.setSpanTags(spanTags);
        refinedSpanList.add(refinedSpan);

        refinedSpan = new RefinedSpan();
        context = new TSpanContext();
        refinedSpan.setContext(context);
        extraContext = new RefinedSpanExtraContext();
        refinedSpan.setExtraContext(extraContext);
        context.setAppId("2");
        context.setTierId("2");
        context.setInstanceId("2");
        extraContext.setElapsed(3);
        extraContext.setSpanTransactionName("/uri2");
        context.setTraceId(traceId);
        context.setSpanId("4");
        context.setParentSpanId("3");
        spanTags = new HashMap<>();
        spanTags.put(FlinkJobConstants.REFINED_SPAN_COMPONENT_DESTINATION, "HTTP");
        refinedSpan.setSpanTags(spanTags);
        refinedSpanList.add(refinedSpan);

        refinedSpanIterable = refinedSpanList;

        oneSpan = new ArrayList<>();

        refinedSpan = new RefinedSpan();
        context = new TSpanContext();
        refinedSpan.setContext(context);
        extraContext = new RefinedSpanExtraContext();
        refinedSpan.setExtraContext(extraContext);
        context.setAppId("2");
        context.setTierId("2");
        context.setInstanceId("2");
        extraContext.setElapsed(2);
        extraContext.setSpanTransactionName("/uri2");
        context.setTraceId(traceId);
        context.setSpanId("5");
        context.setParentSpanId("-1");
        spanTags = new HashMap<>();
        spanTags.put(FlinkJobConstants.REFINED_SPAN_COMPONENT_DESTINATION, "HTTP");
        refinedSpan.setSpanTags(spanTags);
        oneSpan.add(refinedSpan);

    }

    @Test
    public void testAgentTransactionNameComputation() {
        TraceComputeAssistor.computeTrace(refinedSpanIterable, 1, 2);
        for (RefinedSpan refinedSpan : refinedSpanIterable) {
            String spanId = refinedSpan.getContext().getSpanId();
            if ("4".equals(spanId)) {
                Assert.assertEquals(agent2TransactionName, refinedSpan.getExtraContext().getAgentTransactionName());
            } else {
                Assert.assertEquals(agent1TransactionName, refinedSpan.getExtraContext().getAgentTransactionName());
            }
        }
    }

    @Test
    public void testChildIdsAndRootComputation() {
        TraceComputeAssistor.computeTrace(refinedSpanIterable, 1, 2);
        for (RefinedSpan refinedSpan : refinedSpanIterable) {
            String spanId = refinedSpan.getContext().getSpanId();
            if ("1".equals(spanId) || "4".equals(spanId)) {
                Assert.assertTrue(refinedSpan.getExtraContext().isAppRoot());
                Assert.assertTrue(refinedSpan.getExtraContext().isTierRoot());
                Assert.assertTrue(refinedSpan.getExtraContext().isInstanceRoot());
            } else {
                Assert.assertFalse(refinedSpan.getExtraContext().isAppRoot());
                Assert.assertFalse(refinedSpan.getExtraContext().isTierRoot());
                Assert.assertFalse(refinedSpan.getExtraContext().isInstanceRoot());
            }
            if ("3".equals(spanId)) {
                Assert.assertTrue(refinedSpan.getExtraContext().getChildAppId().equals("2"));
                Assert.assertTrue(refinedSpan.getExtraContext().getChildTierId().equals("2"));
                Assert.assertTrue(refinedSpan.getExtraContext().getChildInstanceId().equals("2"));
            }
        }
        TraceComputeAssistor.computeTrace(oneSpan, 1, 2);
        Assert.assertTrue(oneSpan.get(0).getExtraContext().isAppRoot());
        Assert.assertTrue(oneSpan.get(0).getExtraContext().isTierRoot());
        Assert.assertTrue(oneSpan.get(0).getExtraContext().isInstanceRoot());
    }

    @Test
    public void testElapsedComputation() {
        TraceComputeAssistor.computeTrace(refinedSpanIterable, 1, 2);
        for (RefinedSpan refinedSpan : refinedSpanIterable) {
            String spanId = refinedSpan.getContext().getSpanId();
            RefinedSpanExtraContext extraContext = refinedSpan.getExtraContext();
            if ("1".equals(spanId)) {
                Assert.assertTrue(extraContext.getSelfElapsed() == 2);
            } else if ("4".equals(spanId)) {
                Assert.assertTrue(extraContext.getSelfElapsed() == 3);
            } else if ("3".equals(spanId)) {
                Assert.assertTrue(extraContext.getSelfElapsed() == 4);
            } else {
                Assert.assertTrue(extraContext.getSelfElapsed() == 1);
            }
        }

        TraceComputeAssistor.computeTrace(oneSpan, 1, 2);
        Assert.assertTrue(oneSpan.get(0).getExtraContext().getSelfElapsed() == 2);
    }

    @Test
    public void testCopy() throws Exception {
        TSpan span = new TSpan();
        span.setFinishTime(1);
        span.setContext(new TSpanContext());
        span.getContext().setInstanceId("1234");

        ThreadLocalSerializerFactory<TSerializer> serializerFactory =
                new ThreadLocalSerializerFactory(DefaultSerializerFactory.INSTANCE);
        byte[] bytes = serializerFactory.createSerializer().serialize(span);

        ThreadLocalDeserializerFactory<TDeserializer> deserializerFactory =
                new ThreadLocalDeserializerFactory(DefaultDeserializerFactory.INSTANCE);
        RefinedSpan refinedSpan = new RefinedSpan();
        deserializerFactory.createDeserializer().deserialize(refinedSpan, bytes);

        refinedSpan.setExtraContext(new RefinedSpanExtraContext());
        refinedSpan.getExtraContext().setElapsed(123);
        refinedSpan.getExtraContext().setTraceError(true);

        Assert.assertTrue(refinedSpan.getFinishTime() == 1);
        Assert.assertTrue(refinedSpan.getContext().getInstanceId().equals("1234"));

        ThreadLocalSerializerFactory<TSerializer> jsonSerializerFactory =
                new ThreadLocalSerializerFactory(DefaultSimpleJsonSerializerFactory.INSTANCE);
        byte[] jsonBytes = jsonSerializerFactory.createSerializer().serialize(refinedSpan);
        System.out.println(new String(jsonBytes, "UTF-8"));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.PUBLIC_ONLY);

        System.out.println(new String(objectMapper.writeValueAsBytes(refinedSpan), "UTF-8"));

    }



}
