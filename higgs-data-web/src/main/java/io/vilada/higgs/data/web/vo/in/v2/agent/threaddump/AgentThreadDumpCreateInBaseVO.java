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

package io.vilada.higgs.data.web.vo.in.v2.agent.threaddump;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @author yawei
 * @date 2017-12-6.
 */
@Data
public class AgentThreadDumpCreateInBaseVO extends AgentThreadDumpInBaseVO{

    @NotNull
    @Min(value = 1, message = "interval greater than 1")
    @Max(value = 10, message = "interval lesser than 10")
    private Long interval;
}
