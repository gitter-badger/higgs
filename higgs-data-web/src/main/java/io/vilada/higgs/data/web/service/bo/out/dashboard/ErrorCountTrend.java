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

package io.vilada.higgs.data.web.service.bo.out.dashboard;

import java.util.List;

import lombok.Data;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-11-01 17:24
 **/

@Data
public class ErrorCountTrend {

    /**
     *
     */
    List<ErrorCountTrendRow> errorCountTrendRows;

    /**
     * 错误数
     */
    private long errorCount;

    /**
     * 每分钟错误数
     */
    private double epm;


    /**
     * 错误率
     */
    private double errorRate;

}
