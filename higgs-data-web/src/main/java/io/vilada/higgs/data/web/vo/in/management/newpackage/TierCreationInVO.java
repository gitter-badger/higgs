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

package io.vilada.higgs.data.web.vo.in.management.newpackage;

import static io.vilada.higgs.data.service.constants.AgentConfigConstants.HIGGS_AGENT_NAME_RULE;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author nianjun
 * @create 2017-08-27 下午4:11
 **/
@Data
@ApiModel(value = "创建tier所需参数")
public class TierCreationInVO {

    /**
     * tier所属的app的id
     */
    @NotNull(message = "application id can not be null")
    @ApiModelProperty(value = "applicationId", required = true)
    private Long applicationId;

    /**
     * tier的名称
     */
    @NotBlank(message = "name can not be empty")
    @Pattern(regexp = HIGGS_AGENT_NAME_RULE, message = "命名支持中文、数字、字母和下划线,长度不能超过25")
    @ApiModelProperty(value = "tier的名字", required = true)
    private String name;

    /**
     * tier的描述
     */
    @ApiModelProperty(value = "tier的描述")
    private String description;

    /**
     * tier的类型
     */
    @ApiModelProperty(value = "tier的类型", required = true)
    @NotNull(message = "tier type can not be null")
    private Byte tierType;

}
