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

package io.vilada.higgs.data.web.service.elasticsearch.service.v2.database.topn;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import javax.validation.constraints.Min;

@Data
public class DatabaseTopNOutUnit {
    @NotEmpty
    private String groupedFieldValue;
    @NotEmpty
    private String statsFieldValue;
    @Min( value = 0)
    private long count;
    @Min( value = 0)
    private double min;
    @Min( value = 0)
    private double max;
    @Min( value = 0)
    private double avg;
    @Min( value = 0)
    private double sum;
    @Min( value = 0)
    private long cardinality;
}
