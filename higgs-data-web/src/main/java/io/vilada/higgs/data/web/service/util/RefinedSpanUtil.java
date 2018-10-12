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

package io.vilada.higgs.data.web.service.util;

import io.vilada.higgs.data.common.document.RefinedSpanExtraContext;
import io.vilada.higgs.data.common.document.RefinedSpan;
import io.vilada.higgs.data.web.service.enums.SingleTransHealthStatusEnum;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static io.vilada.higgs.data.meta.constants.AgentConfigurationConstants.DEFAULT_APDEX_THRESHOLD;

public class RefinedSpanUtil {

    private RefinedSpanUtil(){}

    public static String getLastUrlInRefererList(RefinedSpan span) {
        if (span == null) {
            return null;
        }

        String transName = getTransactionName(span);
        if (transName == null) {
            return null;
        }

        String httpParam = span.getSpanTags().get("http_param");
        return StringUtils.isEmpty(httpParam)  ? transName : transName + "?" + httpParam;
    }

    private static String getTransactionName(RefinedSpan span) {
        RefinedSpanExtraContext extraContext = span.getExtraContext();
        if (extraContext == null) {
            return null;
        }

        List<String> refererList = extraContext.getSpanRefererList();
        if (CollectionUtils.isEmpty(refererList)) {
            return null;
        }

        int lastIndex = refererList.size() - 1;
        return refererList.get(lastIndex);
    }


    public static SingleTransHealthStatusEnum getSingleTransHealthStatusEnum(RefinedSpan span, int apdexTime) {
        if (apdexTime <= 0) {
            apdexTime = DEFAULT_APDEX_THRESHOLD;
        }

        RefinedSpanExtraContext extraContext = span.getExtraContext();
        int tranElapsed = extraContext.getElapsed();
        double apdexRate = ((double)tranElapsed) / apdexTime;
        return extraContext.isTraceError()
                ? SingleTransHealthStatusEnum.ERROR : SingleTransHealthStatusEnum.getTransStatus(apdexRate);
    }
}
