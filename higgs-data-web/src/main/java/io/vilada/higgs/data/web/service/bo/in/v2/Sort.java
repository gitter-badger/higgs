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
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Pattern;

/**
 * @author pengjunjue
 */
@Data
public class Sort {
	public static final String ORDER_ASC = "asc";

	public static final String ORDER_DESC = "desc";

	@NotBlank (message = "Order field can not be empty!")
	private String field;

	@NotBlank (message = "Order can not be empty!")
	@Pattern(regexp = "^(asc|desc)$", message = "order must be asc or desc")
	private String order;

	public boolean isAsc() {
		return ORDER_ASC.equalsIgnoreCase(order);
	}
}
