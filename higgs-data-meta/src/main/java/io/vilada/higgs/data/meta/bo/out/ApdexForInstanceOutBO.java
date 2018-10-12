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

package io.vilada.higgs.data.meta.bo.out;

import lombok.Data;

/**
 * @author nianjun
 * @date 2018-01-08 17:31
 **/

@Data
public class ApdexForInstanceOutBO {

    /**
     * tier id
     */
    private Long tierId;

    /**
     * tier name
     */
    private String tierName;

    /**
     * instance id
     */
    private Long instanceId;

    /**
     * instance name
     */
    private String instanceName;

    /**
     * apdexT
     */
    private String apdexT;

}
