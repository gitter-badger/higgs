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
import io.vilada.higgs.agent.engine.util.XORShiftRandom;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author mjolnir
 */
public class SamplingProbSampler implements Sampler {

    private final AtomicInteger counter = new AtomicInteger(0);
    private final ProfilerConfig profilerConfig;

    public SamplingProbSampler(ProfilerConfig profilerConfig) {
        this.profilerConfig = profilerConfig;
    }

    public boolean isSampling() {
        int sampleNum = profilerConfig.getSamplingNumerator();
        int sampleDen = profilerConfig.getSamplingDenominator();
        if (sampleNum == sampleDen) {
            return true;
        } else if (sampleNum == 0) {
            return false;
        }
        return XORShiftRandom.random(sampleDen) < sampleNum;
    }


    public String toString() {
        return "SamplingRateSampler{" +
                    "counter=" + counter +
                    "samplingRate=" + profilerConfig.getSamplingNumerator() + "/" +
                    profilerConfig.getSamplingDenominator() +
                '}';
    }
}
