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

package io.vilada.higgs.data.meta.dao.v2.po;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import lombok.Data;

/**
 * Description
 *
 * @author nianjun at 2017-09-26 下午3:28
 **/

@Data
public class Agent {

    private Long id;

    @NotEmpty(message = "empty name is not allowed")
    private String name;

    private String description;

    @NotNull(message = "null type is not allowed")
    private Byte type;

    private Long appId;

    private Long tierId;

    private String token;

    private Integer configVersion;

    private String agentFingerprint;

    private Date lastHealthCheckTime;

    private Byte status;

    private boolean enabled;

    private boolean visible;

    private boolean deleted;

    private Date createTime;

    private Date updateTime;

    /**
     * instance的原始名称
     */
    private String originalName;

}
