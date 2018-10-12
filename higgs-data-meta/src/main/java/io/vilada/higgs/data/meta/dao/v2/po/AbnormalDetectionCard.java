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

package io.vilada.higgs.data.meta.dao.v2.po;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.Date;

/**
 * Created by leigengxin on 2018-1-26.
 */
@Data
public class AbnormalDetectionCard {

    private Long id;

    @NotEmpty(message = "empty latitude is not allowed")
    private String latitude;

    private String nodesId;

    @NotEmpty(message = "empty indicator is not allowed")
    private String indicator;

    @NotEmpty(message = "empty tinyIndicator is not allowed")
    private String tinyIndicator;

    @NotEmpty(message = "empty aggregationTime is not allowed")
    private String aggregationTime;

    @NotEmpty(message = "empty arithmetic is not allowed")
    private String arithmetic;

    @NotEmpty(message = "empty confidence is not allowed")
    private String confidence;

    private String cardName;

    private Date insertTime;

    private Date updateTime;

}