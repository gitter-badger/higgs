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

package io.vilada.higgs.plugin.thrift.interceptor.protocol;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.context.InterceptorContext;
import io.vilada.higgs.agent.common.interceptor.AbstractNonSpanAroundInterceptor;
import io.vilada.higgs.agent.common.trace.HiggsActiveSpan;
import io.vilada.higgs.agent.common.trace.HiggsPropagateHeaderEnum;
import io.vilada.higgs.plugin.thrift.ThriftConstants;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;


public class ProtocolReadTTypeInterceptor extends AbstractNonSpanAroundInterceptor {
    public ProtocolReadTTypeInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext);
    }
       
	@Override
	protected void doBefore(Object target, Object[] args) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doAfter(Object target, Object[] args, Object result, Throwable throwable) {
        HiggsPropagateHeaderEnum header = (HiggsPropagateHeaderEnum)traceContext.getAndRemoveTraceData(ThriftConstants.THRIFT_HEADER_TOBE_READ);
		if (header != null) {
            setTraceHeader(header, result);
		}
	}

	@Override
	protected boolean isPluginEnable(ProfilerConfig profilerConfig) {
        return profilerConfig.readBoolean("higgs.thrift.enable", true);
    }

	public void setTraceHeader(HiggsPropagateHeaderEnum headerKey, Object value) {
        HiggsActiveSpan activeSpan = traceContext.currentActiveSpan();
        if (activeSpan == null) {
            return;
        }
        traceContext.putTraceData(headerKey.name(), byteBufferToString((ByteBuffer)value));
	}

    private static String byteBufferToString(ByteBuffer buf) {
        CharBuffer charBuffer = ThriftConstants.UTF_8.decode(buf);
        return charBuffer.toString();
    }

}
