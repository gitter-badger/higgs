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

package io.vilada.higgs.data.service.bo.in.v2.database;

import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-11-28 16:52
 **/

@Data
public class DataBaseTrendInBO {

    /**
     * 开始时间
     */
    @NotNull(message = "start time can not be null")
    private Long startTime;

    /**
     * 结束时间
     */
    @NotNull(message = "end time can not be null")
    private Long endTime;

    /**
     * interval
     */
    @NotNull(message = "intervalAggr can not be null")
    private Long aggrInterval;

    /**
     * application id
     */
    @NotNull
    private Long appId;

    /**
     * tier id
     */
    private Long tierId;

    /**
     * instance id
     */
    private Long instanceId;

    /**
     * 最小响应时间
     */
    private Long minResponseTime;

    /**
     * 最大响应时间
     */
    private Long maxResponseTime;

    /**
     * 操作方式
     */
    private List<String> operationArray;

    /**
     * 调用者
     */
    private List<String> callerArray;

    /**
     * 数据库类型
     */
    private List<String> databaseTypeArray;


}
