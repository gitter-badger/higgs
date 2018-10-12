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

package io.vilada.higgs.data.meta.dao.entity.newpackage;

import java.util.Date;

import lombok.Data;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-08-25 下午2:13
 **/

@Data
public class Instance {

    /**
     * instance id
     */
    private Long id;

    /**
     * instance name
     */
    private String name;

    /**
     * instance description
     */
    private String description;

    /**
     * instance status
     */
    private Byte status;

    /**
     * whether instance enabled
     */
    private boolean enabled;

    /**
     * instance type
     * 
     * @see io.vilada.higgs.data.meta.enums.AgentConfigTypeEnum
     */
    private Byte type;

    /**
     * instance version
     */
    private Integer configVersion;

    /**
     * time for last health check
     */
    private Date lastHealthCheckTime;

    /**
     * agent token
     */
    private String token;

    /**
     * agent agent fingerprint
     */
    private String agentFingerprint;

    /**
     * instance create time
     */
    private Date createTime;

    /**
     * instance update time
     */
    private Date updateTime;

    /**
     * instance的原始名称
     */
    private String originalName;
}
