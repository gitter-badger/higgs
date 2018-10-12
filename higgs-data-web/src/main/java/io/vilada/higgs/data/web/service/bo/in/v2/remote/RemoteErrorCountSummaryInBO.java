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

package io.vilada.higgs.data.web.service.bo.in.v2.remote;

import io.vilada.higgs.data.web.service.bo.in.v2.common.BaseQueryInBO;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author zhouqi on 2017-11-16 14:45
 */
@Data
public class RemoteErrorCountSummaryInBO extends BaseQueryInBO{

    @NotBlank(message="address object can not be null!")
    private String address;

    @NotNull(message="aggrInterval object can not be null!")
    private Long aggrInterval;

    private Long minResponseTime;

    private Long maxResponseTime;

    private List<String> spanTransaction;

    private List<String> callers;

    private List<String> error;

    private String spanTransactionFilter;


}
