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

package io.vilada.higgs.processing.serialization;

import io.vilada.higgs.processing.HiggsJobContext;
import io.vilada.higgs.processing.dto.AggregateInstance;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.util.serialization.KeyedDeserializationSchema;

import java.io.IOException;

/**
 * @author mjolnir
 */
@Slf4j
public class RefinedSpanAggregateDeserializationSchema implements KeyedDeserializationSchema<AggregateInstance> {

    @Override
    public AggregateInstance deserialize(byte[] messageKey, byte[] message, String topic,
            int partition, long offset) throws IOException {
        try {
            AggregateInstance aggregateInstance = new AggregateInstance();
            HiggsJobContext.getInstance().getDeserializerFactory()
                    .createDeserializer().deserialize(aggregateInstance, message);
            return aggregateInstance;
        } catch (Exception e) {
            log.error("deserialize AggregateInstance failed!", e);
        }
        return null;
    }

    @Override
    public boolean isEndOfStream(AggregateInstance nextElement) {
        return false;
    }

    @Override
    public TypeInformation<AggregateInstance> getProducedType() {
        return TypeInformation.of(AggregateInstance.class);
    }
}
