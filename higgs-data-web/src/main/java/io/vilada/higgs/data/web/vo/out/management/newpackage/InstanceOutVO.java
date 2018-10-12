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

package io.vilada.higgs.data.web.vo.out.management.newpackage;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.vilada.higgs.data.web.vo.util.StringValueSerializer;
import lombok.Data;

/**
 * instance的VO
 *
 * @author nianjun
 * @create 2017-08-22 下午3:17
 **/

@Data
public class InstanceOutVO {

    /**
     * instance id
     */
    @JsonSerialize(using = StringValueSerializer.class)
    private Long id;

    /**
     * instance name
     */
    private String name;

    /**
     * instance status
     */
    private Byte status;

    /**
     * whether instance enabled
     */
    private Boolean enabled;

    /**
     * instance type
     * 
     * @see io.vilada.higgs.data.meta.enums.AgentConfigTypeEnum
     */
    private Byte type;

    /**
     * instance token
     */
    private String token;

    /**
     * instance version
     */
    private Integer configVersion;

    /**
     * instance create time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * instance update time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * time for last health check
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastHealthCheckTime;

    private String address;

    private String originalName;

    private List<String> ips;

    private List<Integer> ports;
}
