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

package io.vilada.higgs.data.web.vo.in.v2.remote;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author zhouqi on 2017-11-30 16:43
 */
@Data
@ApiModel
public class RemoteErrorCondition {

    @ApiModelProperty(required = true)
    @NotBlank(message="appId object can not be null!")
    private String appId;

    @ApiModelProperty(required = true)
    @NotNull(message="startTime object can not be null!")
    private Long startTime;

    @ApiModelProperty(required = true)
    @NotNull(message="endTime object can not be null!")
    private Long endTime;

    @ApiModelProperty(required = true)
    @NotBlank(message="address object can not be null!")
    private String address;

    @ApiModelProperty(required = true)
    @NotNull(message="aggrInterval object can not be null!")
    private Long aggrInterval;

    private Long minResponseTime;

    private Long maxResponseTime;

    private List<String> operationName;

    private List<String> callers;

    private List<String> error;

    private String operationNameFilter;

    private String tierId;

    private String instanceId;

}
