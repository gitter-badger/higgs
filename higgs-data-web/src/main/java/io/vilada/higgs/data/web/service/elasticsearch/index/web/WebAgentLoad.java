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

package io.vilada.higgs.data.web.service.elasticsearch.index.web;

import lombok.Data;
import org.elasticsearch.index.analysis.AnalysisSettingsRequired;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;

import static io.vilada.higgs.data.common.constant.ESIndexConstants.WEB_AGENT_SEARCH_INDEX;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.WEB_LOAD_LOG;

/**
 * Created by caiyp on 2017-07-14.
 */
@Document(indexName = WEB_AGENT_SEARCH_INDEX, type = WEB_LOAD_LOG, createIndex = false)
@AnalysisSettingsRequired
@Data
public class WebAgentLoad {

    @Id
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String id;

    private String instanceId; // required
    private String tierId; // required
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String appId; // required
    private String traceId; // required
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String urlDomain; // required
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String urlQuery;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String browser; // required
    @Field(type = FieldType.Long, index = FieldIndex.not_analyzed)
    private long reportTime; // required
    private long firstScreenTime; // required
    private long whiteScreenTime; // required
    private long operableTime; // required
    private long resourceLoadedTime; // required
    private long loadedTime; // required
    private java.util.Map<String, String> backupProperties; // optional
    private java.util.Map<String, Long> backupQuota; // optional
}
