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

package io.vilada.higgs.data.service.bo.in.v2.remote;

import io.vilada.higgs.data.service.bo.in.v2.common.DurationInBO;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;

import static io.vilada.higgs.data.common.constant.ESIndexConstants.MAX_BUCKET_SIZE;

/**
 * @author ethan
 */
@Getter
@Setter
public class RemoteCallListInBO extends DurationInBO {

    private String tierId;

    private String instanceId;

    @Max(value = MAX_BUCKET_SIZE, message = "The size must be less than 100")
    private int size = MAX_BUCKET_SIZE;

    private String address;

}
