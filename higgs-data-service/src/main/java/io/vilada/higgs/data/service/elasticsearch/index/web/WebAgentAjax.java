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

package io.vilada.higgs.data.service.elasticsearch.index.web;

import lombok.Data;
import org.elasticsearch.index.analysis.AnalysisSettingsRequired;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Map;

import static io.vilada.higgs.data.common.constant.ESIndexConstants.WEB_AGENT_SEARCH_INDEX;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.WEB_AJAX_LOG;

/**
 * Created by Administrator on 2017-5-31.
 */
@Document(indexName = WEB_AGENT_SEARCH_INDEX, type = WEB_AJAX_LOG, createIndex = false)
@AnalysisSettingsRequired
@Data
public class WebAgentAjax {

    @Id
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String id;

    @Field(type = FieldType.String)
    private String instanceId;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String tierId;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String appId;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String traceId;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String urlDomain;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String urlQuery;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String browser;

    @Field(type = FieldType.Long)
    private Long loadedTime;

    @Field(type = FieldType.Long, index = FieldIndex.not_analyzed)
    private Long reportTime;

    private Map<String, String> backupProperties;

    private Map<String, Long> backupQuota;

}
