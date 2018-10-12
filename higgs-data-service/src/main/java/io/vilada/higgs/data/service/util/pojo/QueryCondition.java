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

package io.vilada.higgs.data.service.util.pojo;

import org.elasticsearch.index.query.BoolQueryBuilder;

import io.vilada.higgs.data.common.constant.TypeEnum;
import lombok.Builder;
import lombok.Data;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-12-01 11:31
 **/

@Data
@Builder
public class QueryCondition {

    /**
     * 要添加条件的BoolQueryBuilder
     */
    private BoolQueryBuilder boolQueryBuilder;

    /**
     * application id
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
     * 是否是 root span
     */
    private Boolean rootSpan;

    /**
     * type
     */
    private TypeEnum type;

    /**
     * 开始时间
     */
    private Long startTime;

    /**
     * 是否包含startTime的闭区间
     */
    private boolean includeStartTime;

    /**
     * 结束时间
     */
    private Long endTime;

    /**
     * 是否包含endTime的闭区间
     */
    private boolean includeEndTime;

    /**
     * 目前index对应的类
     */
    private Class clazz;

    /**
     * transactionAggr中的字段,按照instance>tier>app的优先级进行赋值
     */
    private String transactionCategoryId;
}
