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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by lihaiguang on 2017/11/9.
 */
@Data
@ApiModel(value = "仪表盘拓扑的参数")
public class TopologyConditionInVo{

    @NotNull(message = "startTime Can not be null")
    @ApiModelProperty(value = "开始时间", required = true)
    private Long startTime;

    @ApiModelProperty(value = "结束时间", required = true)
    @NotNull(message = "endTime Can not be null")
    private Long endTime;

    @ApiModelProperty(value = "应用ID", required = true)
    @NotEmpty(message = "appId Can not be null")
    private String appId;

    private String transName;
    private String tierId;
    private String instanceId;

    private Integer minResponseTime;
    private List<String> instanceArray;
    private List<String> transTypeArray;
    private List<String> errorTypeArray;
}
