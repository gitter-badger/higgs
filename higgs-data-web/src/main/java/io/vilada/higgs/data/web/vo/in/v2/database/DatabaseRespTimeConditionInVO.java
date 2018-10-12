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

import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * @author zhouqi on 2017-11-30 18:01
 */
@Data
@ApiModel
public class DatabaseRespTimeConditionInVO {
    @ApiModelProperty(required = true)
    @NotBlank(message = "appId should not be null!")
    private String appId;

    @ApiModelProperty(required = true)
    @NotNull(message = "Start time should not be null!")
    @Min(value = 0, message = "startTime should >= 0")
    private Long startTime;

    @ApiModelProperty(required = true)
    @NotNull(message = "End time should not be null!")
    @Min(value = 0, message = "max should >= 0")
    private Long endTime;


    Long[] timeSpanArray;

    private Long minResponseTime;
    private Long maxResponseTime;
    @JsonProperty("operationArray")
    private List<String> operationArray;
    @JsonProperty("callerArray")
    private List<String> callerArray;
    @JsonProperty("databaseTypeArray")
    private List<String> databaseTypeArray;

    private String tierId;

    private String instanceId;
}
