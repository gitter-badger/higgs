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

package io.vilada.higgs.data.web.vo.in.v2.remote;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author yawei
 * @date 2017-11-20.
 */
@Data
public class RemoteSnapshotInVO {

    @NotNull(message = "startTime Can not be null")
    private Long startTime;

    @NotNull(message = "startTime Can not be null")
    private Long endTime;

    @NotBlank(message="appId Can not be null!")
    private String appId;

    @NotBlank(message="address Can not be null!")
    private String address;

    private String tierId;

    private String instanceId;

    private Long minResponseTime;

    private Long maxResponseTime;

    private String transactionFilter;

    private List<String> transaction;

    private List<String> error;

}
