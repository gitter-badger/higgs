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

package io.vilada.higgs.processing.job;

import io.vilada.higgs.processing.FlinkJobConstants;
import io.vilada.higgs.processing.HiggsJobContext;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author mjolnir
 */
public abstract class AbstractHiggsJob {

    protected static StreamExecutionEnvironment configureStreamExecutionEnvironment(
            HiggsJobContext higgsJobContext) {
        return configureStreamExecutionEnvironment(higgsJobContext, higgsJobContext.getJobParallelism());
    }

    protected static StreamExecutionEnvironment configureStreamExecutionEnvironment(
            HiggsJobContext higgsJobContext, int parallelism) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
        env.setParallelism(parallelism);
        env.setMaxParallelism(parallelism * FlinkJobConstants.MAX_PARALLELISM_TIMES);
        if (higgsJobContext.isEnableCheckpoint()) {
            env.enableCheckpointing(higgsJobContext.getCheckpointInterval());
            CheckpointConfig checkpointConfig = env.getCheckpointConfig();
            checkpointConfig.setCheckpointTimeout(higgsJobContext.getCheckpointTimeout());
        }
        return env;
    }
}
