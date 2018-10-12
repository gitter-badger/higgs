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

package io.vilada.higgs.data.web.vo.in.v2.database;

import io.vilada.higgs.data.web.vo.in.BasicLayerIdVO;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author Gerald Kou
 * @date 2017-11-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "Database Condition", description = "数据库访问信息的查询条件")
public class DatabaseConditionInVO extends BasicLayerIdVO {

    private Long minResponseTime;
    private Long maxResponseTime;

    @JsonProperty("operationArray")
    @ApiModelProperty(notes = "DB Operation Types for query")
    private List<String> operationTypes;

    @JsonProperty("callerArray")
    @ApiModelProperty(notes = "DB Callers for query")
    private List<String> callers;

    @JsonProperty("databaseTypeArray")
    @ApiModelProperty(notes = "DB Urls for query")
    private List<String> databaseUrls;

}
