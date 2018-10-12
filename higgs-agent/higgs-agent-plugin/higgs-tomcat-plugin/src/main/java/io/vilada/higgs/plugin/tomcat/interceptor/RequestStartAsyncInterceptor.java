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

package io.vilada.higgs.plugin.tomcat.interceptor;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.context.InterceptorContext;
import io.vilada.higgs.agent.common.context.TraceContext;
import io.vilada.higgs.agent.common.interceptor.AbstractNonSpanAroundInterceptor;
import io.vilada.higgs.agent.common.logging.HiggsAgentLogger;
import io.vilada.higgs.agent.common.logging.HiggsAgentLoggerFactory;
import io.vilada.higgs.agent.common.trace.AsyncAccessor;
import io.vilada.higgs.agent.common.trace.HiggsActiveSpan;
import io.vilada.higgs.agent.common.trace.HiggsContinuationAccessor;

/**
 * @author mjolnir
 */
public class RequestStartAsyncInterceptor extends AbstractNonSpanAroundInterceptor {

    private static HiggsAgentLogger LOGGER = HiggsAgentLoggerFactory.getLogger(RequestStartAsyncInterceptor.class);

    private TraceContext traceContext;

    public RequestStartAsyncInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext);
        this.traceContext = interceptorContext.getTraceContext();
    }

    protected void doBefore(Object target, Object[] args) {

    }

    protected void doAfter(Object target, Object[] args, Object result, Throwable throwable) {
        final HiggsActiveSpan activeSpan = traceContext.currentActiveSpan();
        if (activeSpan == null) {
            return;
        }
        try {
            if (validate(target, result, throwable)) {
                ((AsyncAccessor) target)._$HIGGS$_setAsync(Boolean.TRUE);
                ((HiggsContinuationAccessor) result)._$HIGGS$_setHiggsContinuation(activeSpan.capture());
            }
        } catch (Throwable t) {
            LOGGER.warn("Failed to AFTER process. {}", t.getMessage(), t);
        } finally {
            activeSpan.deactivate();
        }
    }


    protected boolean isPluginEnable(ProfilerConfig profilerConfig) {
        return profilerConfig.readBoolean("higgs.tomcat.enable", true);
    }

    private boolean validate(final Object target, final Object result, final Throwable throwable) {
        if (throwable != null || result == null) {
            return false;
        }

        if (!(target instanceof AsyncAccessor)) {
            LOGGER.debug("Invalid target object. Need field accessor({}).", AsyncAccessor.class.getName());
            return false;
        }

        if (!(result instanceof HiggsContinuationAccessor)) {
            LOGGER.debug("Invalid target object. Need metadata accessor({}).", HiggsContinuationAccessor.class.getName());
            return false;
        }

        return true;
    }
}