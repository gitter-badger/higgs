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

package io.vilada.higgs.data.web.vo.out.agent;

import io.vilada.higgs.data.web.vo.util.StringValueSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.util.Date;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-07-03 下午3:48
 **/

@Data
public class AgentConfigOutVO {

    @JsonProperty("agent_config_id")
    @JsonSerialize(using = StringValueSerializer.class)
    private Long id;

    @JsonProperty("agent_config_name")
    private String name;

    private String token;

    @JsonProperty("agent_config_status")
    private int status;

    @JsonProperty("agent_config_type")
    private Byte type;

    @JsonProperty("agent_config_properties")
    private String properties;

    @JsonProperty("agent_create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
