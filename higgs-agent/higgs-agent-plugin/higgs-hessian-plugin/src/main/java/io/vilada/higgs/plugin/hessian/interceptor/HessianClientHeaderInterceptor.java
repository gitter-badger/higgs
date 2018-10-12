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

package io.vilada.higgs.plugin.hessian.interceptor;

import java.util.Iterator;
import java.util.Map.Entry;
import com.caucho.hessian.client.HessianConnection;
import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.context.InterceptorContext;
import io.vilada.higgs.agent.common.interceptor.AbstractNonSpanAroundInterceptor;
import io.vilada.higgs.common.trace.ComponentEnum;
import io.vilada.higgs.plugin.hessian.HessianProxyAccessor;

/**
 * @author mjolnir
 */
public class HessianClientHeaderInterceptor extends AbstractNonSpanAroundInterceptor {
   
    public HessianClientHeaderInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext);
    }

    protected boolean isPluginEnable(ProfilerConfig profilerConfig) {
        return profilerConfig.readBoolean("higgs.hessian.enable", true);
    }

    protected String getComponentName() {
        return ComponentEnum.HESSIAN_CLIENT.getComponent();
    }	


	@Override
	protected void doBefore(Object target, Object[] args) {
		HessianConnection conn = (HessianConnection)args[0];
		HessianProxyAccessor ha = (HessianProxyAccessor)target;
		try{
		Iterable<Entry<String,String>> headers = ha._$HIGGS$_getHeaders();
		Iterator<Entry<String,String>> it = headers.iterator();
		while(it.hasNext()){
			Entry<String,String> ent = it.next();
			conn.addHeader(ent.getKey(), ent.getValue());
		}
		}finally{		
			ha._$HIGGS$_setHeaders(null);
		}
	}

	@Override
	protected void doAfter(Object target, Object[] args, Object result, Throwable throwable) {
	}
}
