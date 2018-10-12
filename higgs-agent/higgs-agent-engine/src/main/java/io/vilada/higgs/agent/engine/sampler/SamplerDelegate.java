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

package io.vilada.higgs.agent.engine.sampler;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.sampler.Sampler;

import java.util.HashMap;
import java.util.Map;

public class SamplerDelegate implements Sampler {

    private final ProfilerConfig profilerConfig;
    private final Map<String, Sampler> samplerMap;
    private static final String PROB_SAMPLING = "prob";
    private static final String RATE_SAMPLING = "rate";
    public boolean isSampling() {
        String method = profilerConfig.getSamplingMethod();
        Sampler sampler = samplerMap.get(method);
        if (sampler == null) {
            sampler = samplerMap.get(PROB_SAMPLING);
        }
        return sampler.isSampling();
    }

    public SamplerDelegate(ProfilerConfig profilerConfig) {
        this.profilerConfig = profilerConfig;
        this.samplerMap = new HashMap<String, Sampler>();
        samplerMap.put(PROB_SAMPLING, new SamplingProbSampler(profilerConfig));
        samplerMap.put(RATE_SAMPLING, new SamplingRateSampler(profilerConfig));
    }
}
