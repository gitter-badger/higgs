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

package io.vilada.higgs.plugin.thrift.interceptor.server;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.context.InterceptorContext;
import io.vilada.higgs.agent.common.interceptor.AbstractActiveSpanAroundInterceptor;
import io.vilada.higgs.agent.common.logging.HiggsAgentLogger;
import io.vilada.higgs.agent.common.logging.HiggsAgentLoggerFactory;
import io.vilada.higgs.agent.common.trace.HiggsActiveSpan;
import io.vilada.higgs.common.trace.ComponentEnum;
import io.vilada.higgs.common.trace.LayerEnum;
import io.vilada.higgs.common.trace.SpanConstant;
import io.vilada.higgs.plugin.thrift.ThriftConstants;
import io.opentracing.tag.Tags;

/**
 * This interceptor records the response received from the server for synchronous client calls.
 * <p>
 * Based on Thrift 0.8.0+
 * 
 * @author cyp
 */
public class SyncProcessorInterceptor extends AbstractActiveSpanAroundInterceptor {
    private static HiggsAgentLogger LOGGER = HiggsAgentLoggerFactory.getLogger(
            SyncProcessorInterceptor.class);
    private String methodName;

    public SyncProcessorInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext);
        this.methodName = interceptorContext.getMethodDescriptor().getMethodName();
    }

    protected HiggsActiveSpan createHiggsActiveSpan(Object target, Object[] args) {
        HiggsActiveSpan higgsActiveSpan = traceContext.currentActiveSpan();
        if (higgsActiveSpan != null) {
            return higgsActiveSpan;
        }
        StringBuilder builder = new StringBuilder(target.getClass().getCanonicalName());
        builder.append(".");
        builder.append(methodName);
        HiggsActiveSpan activeSpan = traceContext.newActiveSpan(builder.toString(), methodName);
        if (activeSpan != null) {
            activeSpan.setTag(Tags.PEER_SERVICE.getKey(), methodName);
            activeSpan.span().context().setUpdateable(true);
            activeSpan.setTag(SpanConstant.SPAN_COMPONENT_DESTINATION, LayerEnum.RPC.getDesc());
        }
        return activeSpan;
    }

    protected void doBefore(HiggsActiveSpan activeSpan, Object target, Object[] args) {
        activeSpan.setTag(Tags.COMPONENT.getKey(), ComponentEnum.THRIFT_SERVICE.getComponent());
    }

    protected void doAfter(HiggsActiveSpan activeSpan, Object target, Object[] args, Object result, Throwable throwable) {
        try {
            String fnName = (String)traceContext.getAndRemoveTraceData(ThriftConstants.THRIFT_SYNC_FUNC_CALLED);
            if (fnName != null) {
                activeSpan.setOperationName(fnName);
            }
			if(!(throwable instanceof org.apache.thrift.transport.TTransportException)){
				activeSpan.log(throwable);
			}
        } catch (Throwable th) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("AFTER. Caused:{}", th.getMessage(), th);
            }
        } finally {
            activeSpan.deactivate();
        }
    }

    protected boolean isPluginEnable(ProfilerConfig profilerConfig) {
        return profilerConfig.readBoolean("higgs.thrift.enable", true);
	}
}