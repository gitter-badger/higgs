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

package io.vilada.higgs.data.service.bo.in.v2.topology;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author yawei
 * @date 2017-11-25.
 */
@Data
public class TopologyInBO {

    private Long startTime;

    private Long endTime;

    private String appId;

    @NotEmpty(message = "transName can not be empty")
    private String transName;

    private String tierId;

    private String instanceId;

    private Integer minResponseTime;
    private List<String> instanceArray;
    private List<String> transTypeArray;
    private List<String> errorTypeArray;
}
