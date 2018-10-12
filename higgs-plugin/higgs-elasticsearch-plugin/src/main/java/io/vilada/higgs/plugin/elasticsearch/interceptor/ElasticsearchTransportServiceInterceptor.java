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
import io.vilada.higgs.agent.common.interceptor.AbstractSpanAroundInterceptor;
import io.vilada.higgs.agent.common.trace.HiggsSpan;
import io.vilada.higgs.common.trace.ComponentEnum;
import io.vilada.higgs.common.trace.LayerEnum;
import io.vilada.higgs.common.trace.SpanConstant;
import io.vilada.higgs.plugin.elasticsearch.TargetAccessor;
import io.opentracing.tag.Tags;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.cluster.node.DiscoveryNode;

/**
 * @author yawei
 * @date 2018-1-9.
 */
public class ElasticsearchTransportServiceInterceptor extends AbstractSpanAroundInterceptor {

    public ElasticsearchTransportServiceInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext);
    }

    protected void doBefore(HiggsSpan higgsSpan, Object target, Object[] args) {
        DiscoveryNode node = (DiscoveryNode) args[0];
        SearchRequest searchRequest = (SearchRequest) args[2];
        if (node != null && searchRequest instanceof TargetAccessor) {
            TargetAccessor resultTargetAccessor = (TargetAccessor) searchRequest;
            higgsSpan.setTag(Tags.DB_INSTANCE.getKey(), node.getAddress().toString());
            higgsSpan.setTag(Tags.DB_STATEMENT.getKey(), resultTargetAccessor._$HIGGS$_getSourceBuilder().toString());
            higgsSpan.setTag(SpanConstant.SPAN_COMPONENT_TARGET, LayerEnum.NO_SQL.getDesc());
        }
    }

    protected void doAfter(HiggsSpan higgsSpan, Object target, Object[] args, Object result, Throwable throwable) {

    }

    protected String getComponentName() {
        return ComponentEnum.ELASTICSEARCH.getComponent();
    }

    protected boolean isPluginEnable(ProfilerConfig profilerConfig) {
        return profilerConfig.readBoolean("higgs.elasticsearch.enable", true);
    }
}
