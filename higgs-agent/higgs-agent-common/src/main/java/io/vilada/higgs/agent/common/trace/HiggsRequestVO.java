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
 * @author mjolnir
 */
public class HiggsRequestVO {

    private String traceId;

    private String spanId;

    private String spanIndex;

    private String agentToken;

    private String spanReferer;

    private String spanBaggage;

    public HiggsRequestVO(String traceId, String spanId, String spanIndex) {
        this.traceId = traceId;
        this.spanId = spanId;
        this.spanIndex = spanIndex;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public String getSpanIndex() {
        return spanIndex;
    }

    public String getAgentToken() {
        return agentToken;
    }

    public void setAgentToken(String agentToken) {
        this.agentToken = agentToken;
    }

    public String getSpanReferer() {
        return spanReferer;
    }

    public void setSpanReferer(String spanReferer) {
        this.spanReferer = spanReferer;
    }

    public String getSpanBaggage() {
        return spanBaggage;
    }

    public void setSpanBaggage(String spanBaggage) {
        this.spanBaggage = spanBaggage;
    }

}
