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

package io.vilada.higgs.data.common.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;

import static io.vilada.higgs.data.common.constant.ESIndexConstants.ERROR_AGGR_SEARCH_INDEX;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.ONE_MINUTE_TYPE;

/**
 *
 * @author mjolnir
 */
@Document(indexName = ERROR_AGGR_SEARCH_INDEX, type = ONE_MINUTE_TYPE, createIndex = false)
@Data
public class ErrorAggr {

    @Id
    @Field(type = FieldType.String,index = FieldIndex.not_analyzed)
    public String id;

    @Field(type = FieldType.String,index = FieldIndex.not_analyzed)
    public String instanceId;

    @Field(type = FieldType.String,index = FieldIndex.not_analyzed)
    public String tierId;

    @Field(type = FieldType.String,index = FieldIndex.not_analyzed)
    public String appId;

    @Field(type = FieldType.Date)
    public long timeStamp;

    @Field(type = FieldType.String,index = FieldIndex.not_analyzed)
    public String errorType;

    public long errorCount;

}
