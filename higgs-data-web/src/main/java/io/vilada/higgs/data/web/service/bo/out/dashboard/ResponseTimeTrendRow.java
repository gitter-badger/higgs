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

import lombok.Data;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-11-01 17:17
 **/

@Data
public class ResponseTimeTrendRow {

    /**
     * 标题
     */
    private String title;

    /**
     * 时间
     */
    private Long time;

    /**
     * 响应时间
     */
    private long responseTime;

    /**
     * 最大值
     */
    private long maxResponseTime;

    /**
     * 最小值
     */
    private long minResponseTime;

    /**
     * 请求次数
     */
    private long requestCount;
}
