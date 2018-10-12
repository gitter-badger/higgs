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

package io.vilada.higgs.data.web.vo.in.transaction;

import io.vilada.higgs.data.web.vo.in.TimeInVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel
public class ReqCountBarChartInVO extends TimeInVO {

    @ApiModelProperty(required = true)
    @NotBlank(message = "appId should not be null!")
    private String appId;

    @ApiModelProperty(required = true)
    @NotBlank(message = "transName should not be null!")
    private String transName;

    @ApiModelProperty(required = true)
    @NotNull(message = "aggrInterval should not be null!")
    private Long aggrInterval = 3600000L;

    private String dataHistogramInterval;
    private Integer percentageN = 0;
    private String tierId;
    private String instanceId;

    private List<String> instanceArray;
    private Integer minResponseTime;
    private List<String> transTypeArray;
    private List<String> errorTypeArray;


}
