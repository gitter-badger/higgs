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

package io.vilada.higgs.data.service.elasticsearch.service.v2.database.topn;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotEmpty;
import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode
public class DatabaseTopNCondition {
    @Min(value = 0, message = "startTime should >= 0")
    private long startTime;
    @Min(value = 0, message = "startTime should >= 0")
    private long endTime;
    @NotEmpty(message = "application id should not null or empty")
    private String appId;
    private String tierId;
    private String instanceId;
    private Long minResponseTime;
    private Long maxResponseTime;
    @JsonProperty("operationArray")
    private List<String> operationNames;
    @JsonProperty("callerArray")
    private List<String> callers;
    @JsonProperty("databaseTypeArray")
    private List<String> instances;

    public void addOperationName(String operationName) {
        if (operationNames == null) {
            operationNames = new ArrayList<>();
        }
        operationNames.add(operationName);
    }

    public void addCaller(String caller) {
        if (callers == null) {
            callers = new ArrayList<>();
        }
        callers.add(caller);
    }

    public void addInstance(String instance) {
        if (instances == null) {
            instances = new ArrayList<>();
        }
        instances.add(instance);
    }
}
