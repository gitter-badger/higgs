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
import io.vilada.higgs.common.util.MathUtils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author mjolnir
 */
public class SamplingRateSampler implements Sampler {

    private final AtomicInteger counter = new AtomicInteger(0);
    private final ProfilerConfig profilerConfig;

    public SamplingRateSampler(ProfilerConfig profilerConfig) {
        this.profilerConfig = profilerConfig;
    }

    public boolean isSampling() {
        int sampleRate = profilerConfig.getSamplingRate();
        if (sampleRate == 1) {
            return true;
        } else if (sampleRate == 0) {
            return false;
        }
        int samplingCount = MathUtils.fastAbs(counter.getAndIncrement());
        return (samplingCount % sampleRate) == 0;
    }


    public String toString() {
        return "SamplingRateSampler{" +
                    "counter=" + counter +
                    "samplingRate=" + profilerConfig.getSamplingRate() +
                '}';
    }
}
