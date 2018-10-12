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

package io.vilada.higgs.data.web.service.bo.out.transactionchain.overview;

import java.util.List;

import lombok.Data;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-11-01 11:44
 **/

@Data
public class PotentialIssue {

    /**
     * 缓慢组件
     */
    private List<SlowestModule> slowestModule;

    /**
     * 缓慢SQL请求
     */
    private List<SlowestSqlRequest> slowestSqlRequest;

    /**
     * 缓慢远程调用
     */
    private List<SlowestRemoteInvocation> slowestRemoteInvocation;

    /**
     * 错误摘要
     */
    private List<ErrorList> errorSummary;

}
