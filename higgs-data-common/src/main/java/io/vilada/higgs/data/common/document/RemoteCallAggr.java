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

import static io.vilada.higgs.data.common.constant.ESIndexConstants.ONE_MINUTE_TYPE;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.REMOTE_CALL_SEARCH_INDEX;

/**
 *
 * @author mjolnir
 */
@Document(indexName = REMOTE_CALL_SEARCH_INDEX, type = ONE_MINUTE_TYPE, createIndex = false)
@Data
public class RemoteCallAggr {

    @Id
    @Field(type = FieldType.String,index = FieldIndex.not_analyzed)
    public String id;

    @Field(type = FieldType.String,index = FieldIndex.not_analyzed)
    public String instanceId;

    @Field(type = FieldType.String,index = FieldIndex.not_analyzed)
    public String tierId;

    @Field(type = FieldType.String,index = FieldIndex.not_analyzed)
    public String appId;

    @Field(type = FieldType.String,index = FieldIndex.not_analyzed)
    public String address;

    @Field(type = FieldType.String,index = FieldIndex.not_analyzed)
    public String caller;

    @Field(type = FieldType.Date)
    public long timeStamp;

    public long rpm;

    public long epm;

}
