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

package io.vilada.higgs.data.web.vo.in.v2.abnormal;

import io.vilada.higgs.data.web.service.bo.in.v2.common.DurationInBO;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Pattern;

import static io.vilada.higgs.data.web.service.constants.AgentConfigConstants.HIGGS_AGENT_NAME_RULE;

/**
 * Created by leigengxin on 2018-1-26.
 */
@Data
public class AbnormalDetectionCardCreateInVO extends DurationInBO{

    @NotEmpty(message = "latitude 不能为空")
    private String latitude;

    @Length(max = 200, message = "节点id长度不能超过200")
    private String nodesId;

    @NotEmpty(message = "indicator 不能为空")
    private String indicator;

    @NotEmpty(message = "tinyIndicator 不能为空")
    private String tinyIndicator;

    @NotEmpty(message = "aggregationTime 不能为空")
    private String aggregationTime;

    @NotEmpty(message = "arithmetic 不能为空")
    private String arithmetic;

    @NotEmpty(message = "confidence 不能为空")
    private String confidence;

    @Pattern(regexp = HIGGS_AGENT_NAME_RULE, message = "命名支持中文、数字、字母和下划线,长度不能超过25")
    private String cardName;


}