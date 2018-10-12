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

package io.vilada.higgs.data.web.vo.in.database;

import java.util.List;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-11-28 16:52
 **/

@Data
@ApiModel(value = "数据取趋势所需参数")
public class DataBaseTrendInVO {

    /**
     * 开始时间
     */
    @ApiModelProperty(value = "开始时间", required = true)
    @NotNull(message = "start time can not be null")
    private Long startTime;

    /**
     * 结束时间
     */
    @ApiModelProperty(value = "结束时间", required = true)
    @NotNull(message = "end time can not be null")
    private Long endTime;

    /**
     * interval
     */
    @ApiModelProperty(value = "aggregation interval", required = true)
    @NotNull(message = "aggrInterval can not be null")
    private Long aggrInterval;

    /**
     * application id
     */
    @NotNull
    @ApiModelProperty(value = "application id")
    private Long appId;

    /**
     * tier id
     */
    @ApiModelProperty(value = "tier id")
    private Long tierId;

    /**
     * instance id
     */
    @ApiModelProperty(value = "instance id")
    private Long instanceId;

    /**
     * 最小响应时间
     */
    @ApiModelProperty(value = "最小响应时间")
    private Long minResponseTime;

    /**
     * 最大响应时间
     */
    @ApiModelProperty(value = "最大响应时间")
    private Long maxResponseTime;

    /**
     * 操作方式
     */
    @ApiModelProperty(value = "操作方式列表")
    private List<String> operationArray;

    /**
     * 调用者
     */
    @ApiModelProperty(value = "调用者列表")
    private List<String> callerArray;

    /**
     * 数据库类型
     */
    @ApiModelProperty(value = "数据库类型列表")
    private List<String> databaseTypeArray;

}
