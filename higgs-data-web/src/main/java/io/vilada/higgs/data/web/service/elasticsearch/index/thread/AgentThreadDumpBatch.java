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

package io.vilada.higgs.data.web.service.elasticsearch.index.thread;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.List;

/**
 * Created by Administrator on 2017-5-24.
 */

@Data
@Document(indexName = "agent-thread-dump", type = "thread")
public class AgentThreadDumpBatch {

    @Id
    private String id;

    private String agentId;

    private long agentThreadDumpId; // required

    private long startTimestamp; // required

    private long interval; // required

    private List<ThreadDump> threadDumps;

}


