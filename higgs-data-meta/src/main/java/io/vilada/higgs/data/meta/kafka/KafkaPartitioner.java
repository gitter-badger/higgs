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

package io.vilada.higgs.data.meta.kafka;

import io.vilada.higgs.common.util.CollectionUtils;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yawei on 2017-7-20.
 * 自定义分布策略
 */
public class KafkaPartitioner implements Partitioner {

    private final ConcurrentMap<String, AtomicInteger> topicCounterMap = new ConcurrentHashMap<>();

    @Override
    public int partition(String topic, Object o, byte[] bytes, Object o1, byte[] bytes1, Cluster cluster) {
        List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);
        int numPartitions = partitions.size();
        int nextValue = this.nextValue(topic);
        List<PartitionInfo> availablePartitions = cluster.availablePartitionsForTopic(topic);
        if (CollectionUtils.isNotEmpty(availablePartitions)) {
            int part = nextValue % availablePartitions.size();
            return availablePartitions.get(part).partition();
        } else {
            return nextValue % numPartitions;
        }
    }
    private int nextValue(String topic) {
        AtomicInteger counter = this.topicCounterMap.get(topic);
        if (null == counter) {
            counter = new AtomicInteger(0);
            AtomicInteger currentCounter = this.topicCounterMap.putIfAbsent(topic, counter);
            if (currentCounter != null) {
                counter = currentCounter;
            }
        }
        return counter.getAndIncrement() & 0x7fffffff;
    }

    @Override
    public void close() {
        // no need to implements

    }

    @Override
    public void configure(Map<String, ?> map) {
        // no need to implements
    }
}
