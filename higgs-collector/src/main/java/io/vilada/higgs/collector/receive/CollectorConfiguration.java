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

package io.vilada.higgs.collector.receive;

import io.vilada.higgs.collector.receive.config.CollectorConfig;
import io.vilada.higgs.collector.receive.handler.HandlerFactory;
import io.vilada.higgs.collector.receive.receiver.DataReceiver;
import io.vilada.higgs.collector.receive.receiver.http.HTTPReceiver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

import javax.annotation.PreDestroy;

/**
 * @author mjolnir
 */
@EnableKafka
@Configuration
public class CollectorConfiguration {

    private HTTPReceiver httpReceiver;

    @Bean
    public DataReceiver initReceiver(HandlerFactory handlerFactory, CollectorConfig collectorConfig) {
        this.httpReceiver = new HTTPReceiver(handlerFactory, collectorConfig);
        this.httpReceiver.start();
        return this.httpReceiver;
    }

    @PreDestroy
    public void distoryServer() {
        this.httpReceiver.shutdown();
    }

}
