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

package io.vilada.higgs.data.web.vo.out.management.newpackage;

import lombok.Data;

import java.util.List;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-09-07 下午4:49
 **/

@Data
public class ApplicationTierInstanceListVO {

    private List<ApplicationTierInstanceRelationVO> relations;

    private List<ApplicationOutVO> applications;

    private List<TierOutVO> tiers;

    private List<InstanceOutVO> instances;
}
