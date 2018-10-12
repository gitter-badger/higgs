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

package io.vilada.higgs.processing.service.processor;

import io.vilada.higgs.common.util.CollectionUtils;
import io.vilada.higgs.data.common.document.RefinedSpan;
import io.vilada.higgs.data.common.document.RefinedSpanExtraContext;
import io.opentracing.tag.Tags;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * @author mjolnir
 */
public class RpcServerSpanProcessor implements SpanProcessor {

    @Override
    public void processTransactionName(RefinedSpan refinedSpan) {
        RefinedSpanExtraContext spanExtraContext = refinedSpan.getExtraContext();
        String peerService = refinedSpan.getSpanTags().get(Tags.PEER_SERVICE.getKey());
        if (StringUtils.isNotBlank(peerService)) {
            spanExtraContext.setSpanTransactionName(peerService);
            return;
        }

        List<String> spanRefererList = spanExtraContext.getSpanRefererList();
        if (CollectionUtils.isEmpty(spanRefererList)) {
            return;
        }
        spanExtraContext.setSpanTransactionName(spanRefererList.get(spanRefererList.size() - 1));
    }

    @Override
    public void processOperationType(RefinedSpan refinedSpan) {
    }
}
