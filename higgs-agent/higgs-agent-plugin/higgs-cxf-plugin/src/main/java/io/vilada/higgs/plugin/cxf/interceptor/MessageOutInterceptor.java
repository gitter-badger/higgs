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

package io.vilada.higgs.plugin.cxf.interceptor;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.context.InterceptorContext;
import io.vilada.higgs.agent.common.context.TraceContext;
import io.vilada.higgs.agent.common.interceptor.AbstractNonSpanAroundInterceptor;
import io.vilada.higgs.agent.common.trace.HeaderCarrier;
import io.vilada.higgs.agent.common.trace.HiggsActiveSpan;
import io.vilada.higgs.agent.common.trace.HiggsSpan;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.message.Message;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author ethan
 */
public class MessageOutInterceptor extends AbstractNonSpanAroundInterceptor {

    private static final String PARENT_SPAN = "parent_span";

    private TraceContext traceContext;

    public MessageOutInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext);
        this.traceContext = interceptorContext.getTraceContext();
    }


    protected void doBefore(Object target, Object[] args) {
        HiggsSpan parentSpan = (HiggsSpan) traceContext.getAndRemoveTraceData(PARENT_SPAN);
        HiggsActiveSpan higgsActiveSpan = traceContext.currentActiveSpan();
        if (higgsActiveSpan.isDisableTraceDestination() || parentSpan == null) {
            return;
        }
        if (!(args[0] instanceof SoapMessage)) {
            return;
        }
        Message message = (Message) args[0];
        Map<String, List<String>> headers = (Map<String, List<String>>)message.get(Message.PROTOCOL_HEADERS);
        if (headers == null) {
            headers = new HashMap<String, List<String>>();
            message.put(Message.PROTOCOL_HEADERS, headers);
        }

        HeaderCarrier headerCarrier = traceContext.injectCarrier(parentSpan.context());
        Iterator<Map.Entry<String,String>> carrierIterator = headerCarrier.iterator();
        while (carrierIterator.hasNext()) {
            Map.Entry<String,String> carrierEntry = carrierIterator.next();
            headers.put(carrierEntry.getKey(), Arrays.asList(carrierEntry.getValue()));
        }

    }

    protected void doAfter(Object target, Object[] args, Object result, Throwable throwable) {

    }

    protected boolean isPluginEnable(ProfilerConfig profilerConfig) {
        return profilerConfig.readBoolean("higgs.cxf.enable", true);
    }
}
