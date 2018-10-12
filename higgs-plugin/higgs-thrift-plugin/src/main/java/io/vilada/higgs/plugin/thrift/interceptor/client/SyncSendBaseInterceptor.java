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

package io.vilada.higgs.plugin.thrift.interceptor.client;

import io.vilada.higgs.agent.common.context.InterceptorContext;
import io.vilada.higgs.agent.common.trace.HiggsSpan;
import io.vilada.higgs.common.trace.LayerEnum;
import io.vilada.higgs.common.trace.SpanConstant;
import io.vilada.higgs.plugin.thrift.interceptor.ClientSpanAroundInterceptor;
import io.opentracing.tag.Tags;

public class SyncSendBaseInterceptor extends ClientSpanAroundInterceptor {
    public SyncSendBaseInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext);
    }

    @Override
    protected void doBefore(HiggsSpan higgsSpan, Object target, Object[] args) {
        higgsSpan.setTag(SpanConstant.SPAN_COMPONENT_TARGET, LayerEnum.RPC.getDesc());
        if (args != null && args[0] != null) {
            higgsSpan.setTag(Tags.PEER_SERVICE.getKey(), args[0].toString());
        }
    }
}
