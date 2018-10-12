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

import io.vilada.higgs.data.common.document.web.WebAjax;
import io.vilada.higgs.processing.HiggsJobContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.util.serialization.KeyedDeserializationSchema;
import org.apache.thrift.TException;

import java.io.IOException;

/**
 * @author lihaiguang
 */
@Slf4j
public class WebAjaxDeserializationSchema implements KeyedDeserializationSchema<WebAjax> {

    @Override
    public WebAjax deserialize(byte[] messageKey, byte[] message, String topic,
            int partition, long offset) throws IOException {
        try {
            WebAjax webAjax = new WebAjax();
            HiggsJobContext.getInstance().getDeserializerFactory()
                    .createDeserializer().deserialize(webAjax, message);
            return webAjax;
        } catch (TException e) {
            log.error("deserialize message failed!", e);
        }
        return null;
    }

    @Override
    public boolean isEndOfStream(WebAjax nextElement) {
        return false;
    }

    @Override
    public TypeInformation<WebAjax> getProducedType() {
        return TypeInformation.of(WebAjax.class);
    }
}