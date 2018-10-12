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

import lombok.Data;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-11-08 15:08
 **/

@Data
public class ApplicationDashboardInVO {

    /**
     * start time
     */
    @NotNull(message = "start time can not be null")
    private Long startTime;

    /**
     * end time
     */
    @NotNull(message = "end time can not be null")
    private Long endTime;

    /**
     * application id
     */
    @NotNull(message = "application id can not be null")
    private Long appId;

    /**
     * aggr interval
     */
    @NotNull(message = "interval can not be null")
    private Long aggrInterval;

    /**
     * tier id
     */
    private Long tierId;

    /**
     * instance id
     */
    private Long instanceId;

}
