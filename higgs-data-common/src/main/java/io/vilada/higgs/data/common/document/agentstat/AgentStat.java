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

package io.vilada.higgs.data.common.document.agentstat;

import io.vilada.higgs.serialization.thrift.dto.TAgentStat;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.Map;

import static io.vilada.higgs.data.common.constant.ESIndexConstants.AGENTSTAT_SEARCH_INDEX;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.AGENTSTAT_TYPE;

/**
 * @author lihaiguang
 */
@Document(indexName = AGENTSTAT_SEARCH_INDEX, type = AGENTSTAT_TYPE, createIndex = false)
@Data
public class AgentStat extends TAgentStat {

    public String id;

    public long idleThreadCount;

    public Map<String, Long> areaMap;

    public Map<String, Object> jvmGcMap;

}
