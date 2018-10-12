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

package io.vilada.higgs.data.web.vo.in.error;

import io.vilada.higgs.data.service.bo.in.v2.error.FilteredErrorInBO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Size;

/**
 * @author pengjunjue
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class KeyFilteredErrorInVO extends FilteredErrorInBO {
    @Size(max = 100, message = "searchKey must be <= 100")
    private String searchKey;
}
