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

package io.vilada.higgs.agent.common.logging;

import io.vilada.higgs.common.util.logger.CommonLogger;
import io.vilada.higgs.common.util.logger.StdoutCommonLoggerFactory;

/**
 * Obtain logger from agent env,
 * Log info store at agent log dir
 *
 * @author ethan
 */
public final class HiggsAgentLoggerFactory {

    private static HiggsAgentLoggerBinder loggerBinder;

    public static void initialize(HiggsAgentLoggerBinder loggerBinder) {
        if (HiggsAgentLoggerFactory.loggerBinder == null) {
            HiggsAgentLoggerFactory.loggerBinder = loggerBinder;
        } else {
            CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE
                    .getLogger(HiggsAgentLoggerFactory.class.getName());
            logger.warn("loggerBinder is not null");
        }
    }

    public static HiggsAgentLogger getLogger(String name) {
        if (loggerBinder == null) {
            // this prevents null exception: need to return Dummy until a Binder is assigned
            return DummyHiggsAgentLogger.INSTANCE;
        }
        return loggerBinder.getLogger(name);
    }

    public static HiggsAgentLogger getLogger(Class clazz) {
        if (clazz == null) {
            throw new NullPointerException("class must not be null");
        }
        return getLogger(clazz.getName());
    }
}
