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

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import lombok.Data;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-08-27 下午4:11
 **/
@Data
public class TierNameUpdateInVO {

    @NotNull(message = "id can not be null")
    private Long id;

    @Pattern(message = "命名只能包含字母中文数字下划线,必须以字母或中文开头.长度不能超过20", regexp = "^[a-zA-Z\\u4e00-\\u9fa5]?[a-zA-Z\\u4e00-\\u9fa5_0-9]{1,20}$")
    private String name;

}
