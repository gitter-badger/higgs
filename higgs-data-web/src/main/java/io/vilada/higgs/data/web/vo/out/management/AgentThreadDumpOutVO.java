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

package io.vilada.higgs.data.web.vo.out.management;

import io.vilada.higgs.data.web.vo.util.StringValueSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author ethan
 */
@Data
public class AgentThreadDumpOutVO implements Serializable {

    private static final long serialVersionUID = -7820758947344752512L;

    @JsonSerialize(using = StringValueSerializer.class)
    private Long id;

    @JsonProperty("system_id")
    @JsonSerialize(using = StringValueSerializer.class)
    private Long systemId;

    @JsonProperty("agent_id")
    @JsonSerialize(using = StringValueSerializer.class)
    private Long agentId;

    @JsonProperty("agent_token")
    private String agentToken;

    @JsonProperty("agent_name")
    private String agentName;

    @JsonProperty("interval")
    private Long dumpInterval;

    private Integer status;

    @JsonProperty("status_name")
    private String statusName;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm", timezone = "GMT+8")
    @JsonProperty("submit_time")
    private Date submitTime;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm", timezone = "GMT+8")
    @JsonProperty("start_time")
    private Date startTime;

}
