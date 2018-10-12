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

package io.vilada.higgs.processing.service;

import io.vilada.higgs.common.trace.LayerEnum;
import io.vilada.higgs.common.trace.SpanConstant;
import io.vilada.higgs.data.common.constant.RefinedSpanErrorTypeEnum;
import io.vilada.higgs.data.common.constant.TypeEnum;
import io.vilada.higgs.data.common.document.RefinedSpan;
import io.vilada.higgs.data.common.document.RefinedSpanError;
import io.vilada.higgs.data.common.document.RefinedSpanExtraContext;
import io.vilada.higgs.processing.FlinkJobConstants;
import io.vilada.higgs.processing.bo.AgentBO;
import io.vilada.higgs.processing.service.processor.SpanProcessor;
import io.vilada.higgs.processing.service.processor.SpanProcessorFactory;
import io.vilada.higgs.serialization.thrift.dto.TSpanContext;
import io.vilada.higgs.serialization.thrift.dto.TSpanLog;
import io.opentracing.tag.Tags;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.vilada.higgs.common.trace.SpanConstant.SPAN_COMPONENT_DESTINATION;
import static io.vilada.higgs.common.trace.SpanConstant.SPAN_COMPONENT_TARGET;
import static io.vilada.higgs.common.trace.SpanConstant.SPAN_CONTEXT_REFERER_DELIMITER;
import static io.vilada.higgs.common.trace.SpanConstant.SPAN_TAG_PEER_ADDRESS;

/**
 * @author mjolnir
 */
public class TraceTransformer {

    public static void transform(RefinedSpan refinedSpan, AgentBO parentAgent) {
        RefinedSpanExtraContext extraContext = new RefinedSpanExtraContext();
        extraContext.setTransformed(Boolean.TRUE);
        extraContext.setElapsed(Long.valueOf(refinedSpan.getFinishTime() -
                refinedSpan.getStartTime()).intValue());
        refinedSpan.setExtraContext(extraContext);

        transformContext(parentAgent, refinedSpan.getContext(), extraContext);
        transformSpanTags(refinedSpan);
        transformError(refinedSpan);
        transformFiledNameForES(refinedSpan);
    }

    private static void transformContext(AgentBO parentAgent,
            TSpanContext context, RefinedSpanExtraContext extraContext) {
        if (parentAgent != null) {
            extraContext.setParentAppId(parentAgent.getAppId().toString());
            extraContext.setParentTierId(parentAgent.getTierId().toString());
            extraContext.setParentInstanceId(parentAgent.getId().toString());
        }
        String spanRefer = context.getSpanReferer();
        if(!StringUtils.isEmpty(spanRefer)){
            extraContext.setSpanRefererList(Arrays.asList(
                    spanRefer.split(SPAN_CONTEXT_REFERER_DELIMITER)));
        }
    }

    private static void transformSpanTags(RefinedSpan refinedSpan) {
        RefinedSpanExtraContext extraContext = refinedSpan.getExtraContext();
        Map<String, String> spanTagsMap = refinedSpan.getSpanTags();
        extraContext.setComponent(spanTagsMap.get(Tags.COMPONENT.getKey()));

        String componentDestination = spanTagsMap.get(SPAN_COMPONENT_DESTINATION);
        String componentTarget = spanTagsMap.get(SPAN_COMPONENT_TARGET);
        LayerEnum layer = LayerEnum.LIBRARY;
        if (!StringUtils.isEmpty(componentDestination)) {
            layer = LayerEnum.valueOfDesc(componentDestination.toLowerCase());
            extraContext.setType(TypeEnum.SERVER);
        } else if (!StringUtils.isEmpty(componentTarget)) {
            layer = LayerEnum.valueOfDesc(componentTarget.toLowerCase());
            extraContext.setType(TypeEnum.CLIENT);
            extraContext.setAddress(getPeerAddress(spanTagsMap));
        }
        extraContext.setLayer(layer);
        SpanProcessor spanProcessor = SpanProcessorFactory.getSpanProcessor(layer.getDesc());
        spanProcessor.processTransactionName(refinedSpan);
        spanProcessor.processOperationType(refinedSpan);
    }

    private static void transformError(RefinedSpan refinedSpan){
        Map<String, String> spanTagsMap = refinedSpan.getSpanTags();
        String httpStatusCode = spanTagsMap.get(Tags.HTTP_STATUS.getKey());

        TSpanLog spanLog = null;
        List<TSpanLog> spanLogList = refinedSpan.getSpanLogs();
        if (!CollectionUtils.isEmpty(spanLogList)) {
            spanLog = spanLogList.get(0);
        }

        if (httpStatusCode != null && Integer.parseInt(httpStatusCode) >= FlinkJobConstants.HTTP_ERROR_CODE_LOWER) {
            RefinedSpanError refinedSpanCodeError = new RefinedSpanError();
            refinedSpanCodeError.setName(httpStatusCode);
            refinedSpanCodeError.setType(RefinedSpanErrorTypeEnum.CODE);
            refinedSpan.setSpanError(refinedSpanCodeError);
        } else if (spanLog != null){
            RefinedSpanError refinedSpanExceptionError = new RefinedSpanError();
            Map<String, String> refinedSpanLogFields = spanLog.getFields();
            refinedSpanExceptionError.setName(refinedSpanLogFields.get(SpanConstant.SPAN_LOG_ERROR_OBJECT));
            refinedSpanExceptionError.setType(RefinedSpanErrorTypeEnum.EXCEPTION);
            refinedSpanExceptionError.setMessage(refinedSpanLogFields.get(SpanConstant.SPAN_LOG_MESSAGE));
            refinedSpanExceptionError.setStack(refinedSpanLogFields.get(SpanConstant.SPAN_LOG_STACK));
            refinedSpan.setSpanError(refinedSpanExceptionError);
        }
    }

    private static void transformFiledNameForES(RefinedSpan refinedSpan) {
        refinedSpan.setSpanTags(replaceMapKey(refinedSpan.getSpanTags()));
        List<TSpanLog> refinedSpanLogList = refinedSpan.getSpanLogs();
        if (CollectionUtils.isEmpty(refinedSpanLogList)) {
            return;
        }
        for(TSpanLog spanLog : refinedSpanLogList){
            spanLog.setFields(replaceMapKey(spanLog.getFields()));
        }
    }

    private static String getPeerAddress(Map<String, String> spanTagsMap) {
        String peerAddress = spanTagsMap.get(SPAN_TAG_PEER_ADDRESS);
        if (peerAddress != null) {
            return peerAddress;
        }
        String hostName = spanTagsMap.get(SpanConstant.SPAN_TAG_HTTP_HOST);
        String port = spanTagsMap.get(SpanConstant.SPAN_TAG_HTTP_PORT);
        if (hostName == null) {
            hostName = spanTagsMap.get(Tags.PEER_HOSTNAME.getKey());
            port = spanTagsMap.get(Tags.PEER_PORT.getKey());
        }
        if (hostName == null) {
            return null;
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(hostName);
            if (port != null) {
                stringBuilder.append(FlinkJobConstants.SEMICOLON_DELIMITER).append(port);
            }
            return stringBuilder.toString();
        }
    }

    private static Map<String, String> replaceMapKey(Map<String, String> map) {
        if (CollectionUtils.isEmpty(map)) {
            return map;
        }
        Map<String, String> newMap = new HashMap<>();
        for(Map.Entry<String, String> entry : map.entrySet()){
            newMap.put(entry.getKey().replaceAll(FlinkJobConstants.ES_NEED_REPLACE_FIELD_PATTERN,
                    FlinkJobConstants.ES_REPLACEED_FIELD_PATTERN), entry.getValue());
        }
        return newMap;
    }
}
