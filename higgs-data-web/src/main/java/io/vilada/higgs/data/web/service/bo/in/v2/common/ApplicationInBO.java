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

package io.vilada.higgs.data.web.service.bo.in.v2.common;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @author Junjie Peng
 * @date 2018-2-1
 */
@Data
public class ApplicationInBO {
    @NotBlank(message = "app id can not be blank")
    private String appId;
}
