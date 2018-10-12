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

package io.vilada.higgs.data.web.service.bo.out.v2.database;

import lombok.Data;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-11-28 17:18
 **/

@Data
public class DatabaseSqlElapsedTrendData {

    /**
     * 标题
     */
    private String title;

    /**
     * 时间
     */
    private Long time;

    /**
     * 请求次数
     */
    private Long requestCount;

    /**
     * SQL耗时
     */
    private Double sqlElapsed;

    /**
     * 最大SQL耗时
     */
    private Double maxSqlElapsed;

    /**
     * 最小SQL耗时
     */
    private Double minSqlElapsed;

}
