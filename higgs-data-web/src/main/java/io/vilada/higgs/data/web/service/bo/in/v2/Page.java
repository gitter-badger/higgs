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

package io.vilada.higgs.data.web.service.bo.in.v2;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @author pengjunjue
 */
@Data
public class Page {

	@Min(value = 1, message = "size must be >= 1")
	@NotNull(message = "page size must be not null")
	private Integer size = 10;

	@Min(value = 0, message = "index must be >= 0")
	@NotNull(message = "page index must be not null")
	private Integer index = 0;
}
