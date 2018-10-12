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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author yawei
 * @date 2017-11-30.
 */
@Data
@ApiModel(value = "transaction snapshot topology Condition", description = "单次调用链拓扑查询条件")
public class SnapshotTopologyInVO extends SnapshotInVO{

    @NotNull(message = "appId can not be null")
    @ApiModelProperty(notes = "application id for transaction snapshot topology query",required = true)
    private String appId;

}
