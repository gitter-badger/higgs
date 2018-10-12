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

package io.vilada.higgs.processing.service.processor;

import io.vilada.higgs.common.trace.LayerEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mjolnir
 */
public class SpanProcessorFactory {

    private static Map<String, SpanProcessor> PROCESSOR_MAP = new HashMap<>();

    private static OtherSpanProcessor OTHER_PROCESSOR = new OtherSpanProcessor();

    static {
        PROCESSOR_MAP.put(LayerEnum.HTTP.getDesc(), new HttpServerSpanProcessor());
        PROCESSOR_MAP.put(LayerEnum.RPC.getDesc(), new RpcServerSpanProcessor());
        PROCESSOR_MAP.put(LayerEnum.SQL.getDesc(), new DatabaseSpanProcessor());
        PROCESSOR_MAP.put(LayerEnum.NO_SQL.getDesc(), new NosqlServerSpanProcessor());
        PROCESSOR_MAP.put(LayerEnum.CACHE.getDesc(), new CacheServerSpanProcessor());
        PROCESSOR_MAP.put(LayerEnum.MQ.getDesc(), new MqServerSpanProcessor());
        PROCESSOR_MAP.put(LayerEnum.LIBRARY.getDesc(), new RemoteCallClientSpanProcessor());
    }

    public static SpanProcessor getSpanProcessor(String layerDesc) {
        SpanProcessor spanProcessor = PROCESSOR_MAP.get(layerDesc);
        if (spanProcessor != null) {
            return spanProcessor;
        }
        return OTHER_PROCESSOR;
    }

}
