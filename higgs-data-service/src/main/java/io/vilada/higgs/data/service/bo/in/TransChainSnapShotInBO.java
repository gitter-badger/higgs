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

package io.vilada.higgs.data.service.bo.in;

import io.vilada.higgs.data.service.bo.in.v2.common.ApplicationInBO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotBlank;

/**
 * TransChainSnapshotInBO
 * author: zhouqi
 * date: 2017-11-8
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TransChainSnapShotInBO extends ApplicationInBO{
    private String tierId;
    private String instanceId;
    @NotBlank(message = "traceId can not be empty")
    private String traceId;
    @NotBlank(message = "requestId can not be empty")
    private String requestId;
}
