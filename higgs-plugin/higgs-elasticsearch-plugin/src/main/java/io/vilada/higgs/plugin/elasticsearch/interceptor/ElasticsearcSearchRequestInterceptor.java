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

package io.vilada.higgs.plugin.elasticsearch.interceptor;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.context.InterceptorContext;
import io.vilada.higgs.agent.common.interceptor.AbstractNonSpanAroundInterceptor;
import io.vilada.higgs.plugin.elasticsearch.TargetAccessor;
import org.elasticsearch.search.builder.SearchSourceBuilder;

/**
 * @author yawei
 * @date 2018-1-10.
 */
public class ElasticsearcSearchRequestInterceptor extends AbstractNonSpanAroundInterceptor {

    public ElasticsearcSearchRequestInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext);
    }

    protected void doBefore(Object target, Object[] args) {
        if (target instanceof TargetAccessor) {
            TargetAccessor resultTargetAccessor = (TargetAccessor) target;
            resultTargetAccessor._$HIGGS$_setSourceBuilder((SearchSourceBuilder) args[0]);
        }
    }

    protected void doAfter(Object target, Object[] args, Object result, Throwable throwable) {
    }

    protected boolean isPluginEnable(ProfilerConfig profilerConfig) {
        return profilerConfig.readBoolean("higgs.elasticsearch.enable", true);
    }
}
