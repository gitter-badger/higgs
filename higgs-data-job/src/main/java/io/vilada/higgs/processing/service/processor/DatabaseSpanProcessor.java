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

import io.vilada.higgs.data.common.constant.DatabaseOpretationTypeEnum;
import io.vilada.higgs.data.common.document.RefinedSpan;
import io.vilada.higgs.data.common.document.RefinedSpanExtraContext;
import io.opentracing.tag.Tags;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @author ethan
 */
public class DatabaseSpanProcessor implements SpanProcessor {

    @Override
    public void processTransactionName(RefinedSpan refinedSpan) {
        Map<String, String> spanTags = refinedSpan.getSpanTags();
        RefinedSpanExtraContext spanExtraContext = refinedSpan.getExtraContext();
        spanExtraContext.setSpanTransactionName(spanTags.get(Tags.DB_STATEMENT.getKey()));
        spanExtraContext.setAddress(spanTags.get(Tags.DB_INSTANCE.getKey()));
    }

    @Override
    public void processOperationType(RefinedSpan refinedSpan) {
        String sqlStatement = refinedSpan.getSpanTags().get(Tags.DB_STATEMENT.getKey());
        if (StringUtils.isEmpty(sqlStatement)) {
            return;
        }
        sqlStatement = sqlStatement.trim();
        RefinedSpanExtraContext spanExtraContext = refinedSpan.getExtraContext();
        spanExtraContext.setSpanTransactionName(sqlStatement);
        int firstKeywordIndex = sqlStatement.indexOf(' ');
        if (firstKeywordIndex != -1) {
            spanExtraContext.setOperationType(DatabaseOpretationTypeEnum.valueOfByDesc(
                    sqlStatement.substring(0, firstKeywordIndex)).getDesc());
        }
    }
}
