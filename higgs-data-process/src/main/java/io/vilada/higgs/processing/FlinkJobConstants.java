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

package io.vilada.higgs.processing;

/**
 * @author mjolnir
 */
public interface FlinkJobConstants {

    int SINK_PARALLELISM = 1;

    int MAX_PARALLELISM_TIMES = 3;

    String JOB_CHECKPOINT_ENABLE_FIELD = "higgs.job.checkpoint.enable";

    String JOB_DEFAULT_PARALLELISM_FIELD = "higgs.job.default.parallelism";

    String JOB_RAWSPAN_PARALLELISM_FIELD = "higgs.job.rawspan.parallelism";

    String JOB_RAWSPAN_LATENCY_PARALLELISM_FIELD = "higgs.job.rawspan.latency.parallelism";

    String JOB_WEB_PARALLELISM_FIELD = "higgs.job.web.parallelism";

    String JOB_CHECKPOINT_INTERVAL_FIELD = "higgs.job.checkpoint.interval";

    String JOB_CHECKPOINT_TIMEOUT_FIELD = "higgs.job.checkpoint.timeout";

    String JOB_RAWSPAN_WINDOW_SIZE_FIELD = "higgs.rawspan.window.size";

    String JOB_LATENCY_RAWSPAN_WINDOW_SIZE_FIELD = "higgs.rawspan.latency.window.size";

    String JOB_HIGH_LATENCY_RAWSPAN_WINDOW_SIZE_FIELD = "higgs.rawspan.high.latency.window.size";

    String JOB_UNUSUAL_LATENCY_RAWSPAN_WINDOW_SIZE_FIELD = "higgs.rawspan.unusual.latency.window.size";

    String JOB_REFINEDSPAN_AGGREGATE_WINDOW_SIZE_FIELD = "higgs.refinedspan.aggregate.window.size";

    String JOB_ROCESSING_AGENTSTAT_WINDOW_SIZE_FIELD = "higgs.agentstat.processing.window.size";

    String JOB_PROCESSING_AGENTINFO_WINDOW_SIZE_FIELD = "higgs.agentinfo.processing.window.size";

    String JOB_PROCESSING_THREADDUMP_WINDOW_SIZE_FIELD = "higgs.threaddump.processing.window.size";

    String JOB_PROCESSING_WEB_WINDOW_SIZE_FIELD = "higgs.web.processing.window.size";

    String JOB_REPROCESSING_RAWSPAN__WINDOW_SIZE_FIELD = "higgs.rawspan.reprocessing.window.size";

    String JOB_REFINEDSPAN_MAX_PROCESS_TIMES_FIELD = "higgs.rawspan.process.max.times";

    String JOB_KAFKA_SERVER_FIELD = "higgs.kafka.bootstrap-servers";

    String JOB_KAFKA_SERIALIZER_FIELD = "higgs.kafka.serializer";

    String JOB_KAFKA_COMPRESSION_FIELD = "higgs.kafka.compression.type";

    String JOB_KAFKA_RAWSPAN_TOPIC_FIELD = "higgs.kafka.rawspan.topic";

    String JOB_KAFKA_RAWSPAN_REPROCESSING_TOPIC_FIELD = "higgs.kafka.rowspan.reprocessing.topic";

    String JOB_KAFKA_RAWSPAN_LATENCY_TOPIC_FIELD = "higgs.kafka.rowspan.latency.topic";

    String JOB_KAFKA_RAWSPAN_HIGH_LATENCY_TOPIC_FIELD = "higgs.kafka.rowspan.high.latency.topic";

    String JOB_KAFKA_RAWSPAN_UNUSUAL_LATENCY_TOPIC_FIELD = "higgs.kafka.rowspan.unusual.latency.topic";

    String JOB_KAFKA_REFINEDSPAN_AGGREGATE_TOPIC_FIELD = "higgs.kafka.refinedspan.aggregate.topic";

    String JOB_KAFKA_GROUP_FIELD = "higgs.kafka.consumer.group";

    String JOB_KAFKA_AGENT_INFO_TOPIC_FIELD = "higgs.kafka.agent.info.topic";

    String JOB_KAFKA_AGENT_STAT_TOPIC_FIELD = "higgs.kafka.agent.stat.topic";

    String JOB_KAFKA_THEAD_DUMP_TOPIC_FIELD = "higgs.kafka.thread.dump.topic";

    String JOB_KAFKA_WEB_LOAD_TOPIC_FIELD = "higgs.kafka.web.load.topic";

    String JOB_KAFKA_WEB_AJAX_TOPIC_FIELD = "higgs.kafka.web.ajax.topic";

    String JOB_ES_CLUSTER_NODE_FIELD = "higgs.elasticsearch.cluster-nodes";

    String JOB_ES_CLUSTER_NODE_NAME_FIELD = "higgs.elasticsearch.cluster-name";

    String JOB_ES_BULK_SIZE_FIELD = "higgs.elasticsearch.bulk.size";

    String JOB_ES_FLUSH_INTERVAL_FIELD = "higgs.elasticsearch.flush.interval.ms";

    String KAFKA_BROKER_SERVER_CONFIG_FIELD = "bootstrap.servers";

    String KAFKA_CONSUMER_GROUP_CONFIG_FIELD = "group.id";

    String KAFKA_KEY_SERIALIZER_FIELD = "key.serializer";

    String KAFKA_VALUE_SERIALIZER_FIELD = "value.serializer";

    String KAFKA_COMPRESSION_FIELD = "compression.type";

    String KAFKA_DEFAULT_COMPRESSION_TYPE = "lz4";

    String ES_CLUSTER_NAME_CONFIG_FIELD = "cluster.name";

    String ES_BULK_SIZE_CONFIG_FIELD = "bulk.flush.max.actions";

    String ES_INTERVAL_MS_CONFIG_FIELD = "bulk.flush.interval.ms";

    String ES_REFINEDSPAN_INCOMPLETE_SIZE_FIELD = "higgs.elasticsearch.refinedspan.incomplete.size";

    int HTTP_ERROR_CODE_LOWER = 400;

    String EMPTY_STRING = "";

    String COMMA_DELIMITER = ",";

    String SEMICOLON_DELIMITER = ":";

    String DASH_DELIMITER = "-";

    String ES_NEED_REPLACE_FIELD_PATTERN = "\\.";

    String ES_REPLACEED_FIELD_PATTERN = "_";

    String CONTEXT_TRACE_ID = "context.traceId";

    int INCOMPLETE_FROM = 0;

    String REFINED_SPAN_COMPONENT_TARGET = "component_target";

    String REFINED_SPAN_COMPONENT_DESTINATION = "component_destination";

    int LOOP_DEEP_PROTECT_SIZE = 100;

}
