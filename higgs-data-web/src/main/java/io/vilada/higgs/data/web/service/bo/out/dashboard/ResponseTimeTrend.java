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
 * @create 2017-11-01 17:17
 **/

@Data
public class ResponseTimeTrend {

    /**
     * 响应时间数据表
     */
    private List<ResponseTimeTrendRow> responseTimeTrendRowList;

    /**
     * P90数据表
     */
    private List<P90TrendRow> p90TrendRowList;

    /**
     * 平均响应时间
     */
    private double avgResponseTime;

    /**
     * apdex
     */
    private double apdex;

    @Data
    public static class P90TrendRow {
        /**
         * 标题
         */
        private String title;

        /**
         * 时间
         */
        private Long time;

        /**
         * p90值
         */
        private long p90Time;
    }
}
