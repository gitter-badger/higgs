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

package io.vilada.higgs.data.web.vo.in.v2.application;

import static io.vilada.higgs.data.web.service.constants.AgentConfigConstants.HIGGS_AGENT_NAME_RULE;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;

/**
 * @author nianjun
 * @create 2017-12-29
 */

@Data
public class ApplicationCreateInVO {

    @NotBlank(message = "name不能为空")
    @Pattern(regexp = HIGGS_AGENT_NAME_RULE, message = "命名支持中文、数字、字母和下划线,长度不能超过25")
    private String name;


    @Length(max = 200, message = "描述的长度不能超过200")
    private String description;


}
