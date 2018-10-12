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

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;

/**
 * @author zhouqi on 2017-11-29 10:51
 */
@Data
public class DatabaseRespTimeSummaryInBO {

    @NotBlank(message = "appId should not be null!")
    private String appId;

    @NotNull(message = "Start time should not be null!")
    @Min(value = 0, message = "startTime should >= 0")
    private Long startTime;

    @NotNull(message = "End time should not be null!")
    @Min(value = 0, message = "max should >= 0")
    private Long endTime;


    Long[] timeSpanArray;

    private Long minResponseTime;
    private Long maxResponseTime;
    private List<String> operationArray;
    private List<String> callerArray;
    private List<String> databaseTypeArray;
    private String tierId;

    private String instanceId;
}
