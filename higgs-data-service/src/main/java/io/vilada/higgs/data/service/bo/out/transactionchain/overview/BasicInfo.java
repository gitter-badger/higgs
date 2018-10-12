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

package io.vilada.higgs.data.service.bo.out.transactionchain.overview;

import lombok.Data;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-11-01 11:27
 **/

@Data
public class BasicInfo {

    /**
     * 用户体验
     */
    private String userExperience;

    /**
     * 事务
     */
    private String transaction;

    /**
     * 响应时间
     */
    private long responseTime;

    /**
     * 发生时间
     */
    private long occurTime;

    /**
     * 实例信息
     */
    private String instanceInfo;

    /**
     * 层
     */
    private String tierName;

    /**
     * 线程名称
     */
    private String threadName;

    /**
     * 事务类型
     */
    private String transactionType;
}
