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
import io.vilada.higgs.agent.common.logging.HiggsAgentLogger;
import io.vilada.higgs.agent.common.logging.HiggsAgentLoggerFactory;
import io.vilada.higgs.agent.common.trace.HeaderCarrier;
import io.vilada.higgs.agent.common.trace.HiggsActiveSpan;
import io.vilada.higgs.agent.common.trace.HiggsPropagateHeaderEnum;
import io.vilada.higgs.agent.common.util.Triple;
import io.vilada.higgs.plugin.thrift.ThriftConstants;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TType;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;

public class ProtocolWriteFieldStopInterceptor extends AbstractNonSpanAroundInterceptor {
    private static HiggsAgentLogger logger = HiggsAgentLoggerFactory.getLogger(
            ProtocolWriteFieldStopInterceptor.class);

    public ProtocolWriteFieldStopInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext);
    }
       
	@Override
	protected void doBefore(Object target, Object[] args) {
		if (target instanceof TProtocol) {
			TProtocol oprot = (TProtocol)target;
			try {
				appendParentTraceInfo(oprot);
			} catch (Throwable t) {
				logger.warn("problem writing trace info", t);
			}
		}
	}

	@Override
	protected void doAfter(Object target, Object[] args, Object result, Throwable throwable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean isPluginEnable(ProfilerConfig profilerConfig) {
        return profilerConfig.readBoolean("higgs.thrift.enable", true);
    }

	private void appendParentTraceInfo(TProtocol oprot) throws TException {
		HiggsActiveSpan activeSpan = traceContext.currentActiveSpan();
		if (activeSpan == null) {
		    return;
        }
        HeaderCarrier headerCarrier = traceContext.injectCarrier(activeSpan.span().context());
        Triple<String, Integer, String> header = activeSpan.peekSpanHeader();
        headerCarrier.setSpanId(header.getKey());
        headerCarrier.setSpanIndex(header.getValue1().toString());
        headerCarrier.setSpanReferer(header.getValue2());
        Iterator<Map.Entry<String,String>> carrierIterator = headerCarrier.iterator();
        while (carrierIterator.hasNext()) {
            Map.Entry<String,String> carrierEntry = carrierIterator.next();
            writeTraceHeader(carrierEntry.getKey(), carrierEntry.getValue(), oprot);
        }
	}

    private void writeTraceHeader(String name, String value, TProtocol oprot) throws TException {
        HiggsPropagateHeaderEnum header = HiggsPropagateHeaderEnum.getHeader(name);
        short id = (short)(Short.MIN_VALUE + header.getId());
        TField traceField = new TField(name, TType.STRING, id);
        oprot.writeFieldBegin(traceField);
        try {
            oprot.writeBinary(stringToByteBuffer(value));
        } finally {
            oprot.writeFieldEnd();
        }
    }

    private static ByteBuffer stringToByteBuffer(String s) {
        try {
            return ByteBuffer.wrap(s.getBytes(ThriftConstants.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            return ByteBuffer.wrap(s.getBytes());
        }
    }
}
