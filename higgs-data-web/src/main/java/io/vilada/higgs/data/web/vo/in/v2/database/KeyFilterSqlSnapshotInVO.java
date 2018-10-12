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

package io.vilada.higgs.data.web.vo.in.v2.database;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Size;

/**
 * @author Gerald Kou
 * @date 2017-11-23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "SQL statement Condition", description = "待查询SQL语句条件")
public class KeyFilterSqlSnapshotInVO extends DatabaseConditionInVO {
    @Size(max = 100, message = "size of sqlStatement must be <= 100")
    @JsonProperty("statement")
    @ApiModelProperty(notes = "SQL segment for sql list query")
    private String sqlStatement;
}
