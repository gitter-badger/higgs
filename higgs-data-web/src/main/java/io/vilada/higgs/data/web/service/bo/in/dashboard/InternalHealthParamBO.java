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

package io.vilada.higgs.data.web.service.bo.in.dashboard;

import io.vilada.higgs.data.common.constant.TypeEnum;
import lombok.Builder;
import lombok.Data;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-11-09 20:25
 **/

@Data
@Builder
public class InternalHealthParamBO {

    /**
     * applicationId
     */
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
     * apdexT
     */
    private int apdexT;

    /**
     * start time
     */
    private Long startTime;

    /**
     * end time
     */
    private Long endTime;

    /**
     * es field
     */
    private String field;

    /**
     * terms base on field
     */
    private String termsField;

    /**
     * is root span
     */
    private Boolean rootSpan;

    /**
     * aggr interval
     */
    private Long aggrInterval;

    /**
     * type
     */
    private TypeEnum type;

    /**
     * class
     */
    private Class clazz;

    /**
     * is error
     */
    private Boolean error;

    /**
     * 耗时的字段
     */
    private String elapsedField;
}
