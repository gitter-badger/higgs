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

package io.vilada.higgs.data.web.vo.in;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(value = "Duration Condition", description = "时间段的查询条件")
public class DurationInVO {
    @NotNull(message = "startTime should be assigned")
    @Min(value = 0, message = "startTime should >= 0")
    @ApiModelProperty(notes = "start time of the duration",required = true)
    private Long startTime;

    @NotNull(message = "startTime should be assigned")
    @Min(value = 0, message = "endTime should >= 0")
    @ApiModelProperty(notes = "end time of the duration",required = true)
    private Long endTime;
}