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

package io.vilada.higgs.agent.engine.context.module.provider;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.context.TraceContext;
import io.vilada.higgs.agent.common.sampler.Sampler;
import io.vilada.higgs.agent.engine.context.internal.DefaultTraceContext;
import io.vilada.higgs.agent.engine.transport.DataTransport;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * @author mjolnir
 */
public class TraceContextProvider implements Provider<TraceContext> {

    private final ProfilerConfig profilerConfig;

    private Sampler sampler;

    private DataTransport dataTransport;

    @Inject
    public TraceContextProvider(ProfilerConfig profilerConfig, Sampler sampler,
            DataTransport dataTransport) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        this.profilerConfig = profilerConfig;
        this.sampler = sampler;
        this.dataTransport = dataTransport;
    }

    public TraceContext get() {
        return new DefaultTraceContext(profilerConfig, sampler, dataTransport);
    }
}
