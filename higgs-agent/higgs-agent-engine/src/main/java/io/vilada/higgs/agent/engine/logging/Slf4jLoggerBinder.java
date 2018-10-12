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

package io.vilada.higgs.agent.engine.logging;

import io.vilada.higgs.agent.common.logging.HiggsAgentLogger;
import io.vilada.higgs.agent.common.logging.HiggsAgentLoggerBinder;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author ethan
 */
public class Slf4jLoggerBinder implements HiggsAgentLoggerBinder {

    private ConcurrentMap<String, HiggsAgentLogger> loggerCache = new ConcurrentHashMap<String, HiggsAgentLogger>(256, 0.75f, 128);


    public HiggsAgentLogger getLogger(String name) {

        final HiggsAgentLogger higgsAgentLogger = loggerCache.get(name);
        if (higgsAgentLogger != null) {
            return higgsAgentLogger;
        }

        org.slf4j.Logger slf4jLogger = LoggerFactory.getLogger(name);
        final HiggsSlf4JLoggerDelegate loggerDelegate = new HiggsSlf4JLoggerDelegate(slf4jLogger);
        final HiggsAgentLogger before = loggerCache.putIfAbsent(name, loggerDelegate);
        if (before != null) {
            return before;
        }
        return loggerDelegate;
    }

}
