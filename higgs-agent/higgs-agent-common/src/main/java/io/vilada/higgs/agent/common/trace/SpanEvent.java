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

package io.vilada.higgs.agent.common.trace;

/**
 * @author ethan
 */
public enum SpanEvent {

    TIMEOUT("timeout", null),
    ERROR("error", "Exception"),
    AGENT_STARTED("agent started", null),
    AGENT_PLUGIN_DISABLED("agent plugin disabled", null),
    AGENT_DISABLED("agent disabled", null);


    private String message;
    private String kind;
    SpanEvent(String message, String kind) {
        this.message = message;
        this.kind = kind;
    }

    public String getMessage() {
        return message;
    }

    public String getKind() {
        return kind;
    }
}
