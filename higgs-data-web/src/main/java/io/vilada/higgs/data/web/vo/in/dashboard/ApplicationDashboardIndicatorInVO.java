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

package io.vilada.higgs.data.web.vo.in.dashboard;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-11-08 15:08
 **/

@Data
@ApiModel(value = "仪表盘上指示器的参数")
public class ApplicationDashboardIndicatorInVO {

    @NotNull(message = "start time can not be null")
    @ApiModelProperty(value = "开始时间", required = true)
    private Long startTime;

    @ApiModelProperty(value = "结束时间", required = true)
    @NotNull(message = "end time can not be null")
    private Long endTime;

    @ApiModelProperty(value = "application id", required = true)
    @NotNull(message = "application id can not be null")
    private Long appId;

    private Long tierId;

    private Long instanceId;

}
