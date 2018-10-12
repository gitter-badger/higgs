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

package io.vilada.higgs.data.web.vo.out.management;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.vilada.higgs.data.web.vo.util.StringValueSerializer;
import lombok.Data;

/**
 * Created by Administrator on 2017-5-31.
 */
@Data
public class ThreadDumpOutVO {

    @JsonSerialize(using = StringValueSerializer.class)
    public long id; // required
    public String name; // required
    public Thread.State state; // required
    @JsonProperty("cpu_time")
    public long cpuTime; // required
    @JsonProperty("stack_trace")
    public java.util.List<ThreadStackTraceOutVO> stackTrace;
}
