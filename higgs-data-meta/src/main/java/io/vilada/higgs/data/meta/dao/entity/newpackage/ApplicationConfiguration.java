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

package io.vilada.higgs.data.meta.dao.entity.newpackage;

import lombok.Data;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-08-21 下午5:07
 **/

@Data
public class ApplicationConfiguration {

    private Long id;

    private Long applicationId;

    private String configurationName;

    private String configurationKey;

    private String configurationValue;

    private Byte configurationType;

    private Byte configurationLevel;

    private boolean enabled;

    private String configurationUnit;

}
