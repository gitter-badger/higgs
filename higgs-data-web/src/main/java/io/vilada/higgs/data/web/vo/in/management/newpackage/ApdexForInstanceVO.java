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

import lombok.Data;

/**
 * @author nianjun at 2017-12-25 14:09
 **/

@Data
public class ApdexForInstanceVO {

    @NotNull(message = "instance id can not be null")
    private Long instanceId;

    @NotNull(message = "apdexT can not be null")
    private Long apdexT;

}
