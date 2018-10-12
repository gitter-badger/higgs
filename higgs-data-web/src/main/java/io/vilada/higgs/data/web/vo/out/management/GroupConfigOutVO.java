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

import io.vilada.higgs.data.web.vo.out.agent.AgentConfigOutVO;
import io.vilada.higgs.data.web.vo.util.StringValueSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-07-03 下午3:48
 **/

@Data
public class GroupConfigOutVO {

    @JsonProperty("group_config_id")
    @JsonSerialize(using = StringValueSerializer.class)
    private Long id;

    @JsonProperty("group_config_name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("tier_type")
    private Byte tierType;

    @JsonProperty("agent_config")
    private List<AgentConfigOutVO> agentConfig;

    @JsonProperty("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonProperty("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

}
