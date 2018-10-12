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
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * @author Gerald Kou
 * @date 2017-11-23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "Basic Layered Condition", description = "基础分层信息的查询条件")
public class BasicLayerIdVO extends DurationInVO{
    @NotEmpty(message = "application id should not null or empty")
    @ApiModelProperty(notes = "the id of application",required = true)
    private String appId;

    @ApiModelProperty(notes = "the id of tier")
    private String tierId;

    @ApiModelProperty(notes = "the id of instance")
    private String instanceId;

}
