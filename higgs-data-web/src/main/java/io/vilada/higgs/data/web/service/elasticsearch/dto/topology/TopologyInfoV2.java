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

package io.vilada.higgs.data.web.service.elasticsearch.dto.topology;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yawei on 2017-7-17.
 */
@Data
public class TopologyInfoV2 {
    private List<TopologyDetailInfoV2> nodes = new ArrayList<>();
    private List<TopologyLinkInfoV2> links = new ArrayList<>();
}
