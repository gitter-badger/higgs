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

package io.vilada.higgs.collector.receive.handler;

import io.vilada.higgs.common.util.HiggsMessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ethan
 * @author yawei.liu
 */
@Component
public class HandlerFactory {

    @Autowired
    AgentHealthCheckHandler agentHealthCheckHandler;

    @Autowired
    private AgentInfoHandler agentInfoHandler;

    @Autowired
    private AgentStatHandler agentStatHandler;

    @Autowired
    private SpanHandler spanHandler;

    @Autowired
    private AgentThreadDumpHandler threadDumpHandler;

    @Autowired
    private WebAgentAjaxHandler webAjaxHandler;

    @Autowired
    private WebLoadHandler webLoadHandler;

    private Map<String,Handler> handlerMapFactory = new HashMap<>();

    public Handler getHandler(String type){
        return handlerMapFactory.get(type);
    }

    @PostConstruct
    private void setHandlerMap() {
        handlerMapFactory.put(HiggsMessageType.HEALTHCHECK.toString(), agentHealthCheckHandler);
        handlerMapFactory.put(HiggsMessageType.AGENT.toString(), agentInfoHandler);
        handlerMapFactory.put(HiggsMessageType.AGENT_STAT_BATCH.toString(), agentStatHandler);
        handlerMapFactory.put(HiggsMessageType.SPAN_BATCH.toString(), spanHandler);
        handlerMapFactory.put(HiggsMessageType.THREAD_DUMP_BATCH.toString(), threadDumpHandler);
        handlerMapFactory.put(HiggsMessageType.WEB_AJAX_BATCH.toString(), webAjaxHandler);
        handlerMapFactory.put(HiggsMessageType.WEB_LOAD_BATCH.toString(), webLoadHandler);
    }
}
