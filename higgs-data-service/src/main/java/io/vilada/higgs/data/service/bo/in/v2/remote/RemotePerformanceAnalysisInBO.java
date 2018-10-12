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

package io.vilada.higgs.data.service.bo.in.v2.remote;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-11-20 14:14
 **/

@Data
public class RemotePerformanceAnalysisInBO {

    /**
     * uri
     */
    @NotBlank(message = "address can not be null!")
    private String address;

    /**
     * application id
     */
    private Long appId;

    /**
     * 开始时间
     */
    @NotBlank(message = "startTime can not be null!")
    private Long startTime;

    /**
     * 结束时间
     */
    @NotBlank(message = "endTime can not be null!")
    private Long endTime;

    /**
     * 时间间隔
     */
    @NotNull(message = "interval can not be null!")
    private Long aggrInterval;

    /**
     * 最小响应时间
     */
    private Long minxResponseTime;

    /**
     * 最大响应时间
     */
    private Long maxResponseTime;

    /**
     * 调用者
     */
    private List<String> callers;

    /**
     * 错误类型
     */
    private List<String> errorTypes;

    /**
     * Tier Id
     */
    private Long tierId;

    /**
     * 实例Id
     */
    private Long instanceId;

    /**
     * result size
     */
    private Integer resultSize;

    /**
     * filter error data
     */
    private Boolean filterError;

}
