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

package io.vilada.higgs.data.web.service.elasticsearch.index.agentstat;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;
import java.util.Map;

import static io.vilada.higgs.data.common.constant.ESIndexConstants.AGENTSTAT_SEARCH_INDEX;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.AGENTSTAT_TYPE;

/**
 * @author yawei
 * @date 2017-5-31.
 */
@Document(indexName = AGENTSTAT_SEARCH_INDEX, type = AGENTSTAT_TYPE, createIndex = false)
@Data
public class AgentStat {

    @Id
    private String id;

    @Field(type = FieldType.String,index = FieldIndex.not_analyzed)
    private String instanceId;

    @Field(type = FieldType.String,index = FieldIndex.not_analyzed)
    private String tierId;

    @Field(type = FieldType.String,index = FieldIndex.not_analyzed)
    private String appId;

    @Field(type = FieldType.Date)
    private long timestamp;

    private long collectInterval;

    private long loadedClassCount;

    private long unloadedClassCount;

    private long totalThreadCount;

    private long activeThreadCount;

    private long idleThreadCount;

    @Field(type = FieldType.Object,index = FieldIndex.not_analyzed)
    private JvmMemory memory;

    @Field(type = FieldType.Object,index = FieldIndex.not_analyzed)
    private List<JvmGc> gc;

    private Map<String, Long> areaMap;

    private Map<String, Object> jvmGcMap;

}
