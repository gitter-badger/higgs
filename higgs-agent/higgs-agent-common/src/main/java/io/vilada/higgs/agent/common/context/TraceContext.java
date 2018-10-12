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

package io.vilada.higgs.agent.common.context;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.trace.HeaderCarrier;
import io.vilada.higgs.agent.common.trace.HiggsActiveSpan;
import io.vilada.higgs.agent.common.trace.HiggsSpan;
import io.vilada.higgs.agent.common.trace.HiggsSpanContext;

/**
 * @author mjolnir
 */
public interface TraceContext {

    HiggsActiveSpan newActiveSpan(String operationName, String spanReferer);

    HeaderCarrier injectCarrier(HiggsSpanContext higgsSpanContext);

    HiggsActiveSpan continueActiveSpan(HeaderCarrier headerCarrier,
                                       String operationName, String spanReferer);

    HiggsActiveSpan currentActiveSpan();

    void attach(HiggsActiveSpan higgsActiveSpan);

    HiggsSpan newSpan(String operationName);

    ProfilerConfig getProfilerConfig();

    Object getAndRemoveTraceData(Object key);

    Object putTraceData(Object key, Object value);

}
