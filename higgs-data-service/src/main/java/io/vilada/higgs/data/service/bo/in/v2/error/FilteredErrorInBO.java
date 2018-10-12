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

package io.vilada.higgs.data.service.bo.in.v2.error;

import io.vilada.higgs.data.service.bo.in.v2.common.BaseQueryInBO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author Junjie Peng
 * @date 2017-11-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FilteredErrorInBO extends BaseQueryInBO {
    @Size(max = 20, message = "size of errorArray must be <= 20")
    private List<String> errorArray;

    @Size(max = 20, message = "size of instanceArray must be <= 20")
    private List<String> instanceArray;
}
