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

package io.vilada.higgs.agent.common.trace;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mjolnir
 */
public class HeaderCarrier extends TextMapCarrier {

    public static String BAGGAGE_ITEM_DELIMITER = ";";

    public static String BAGGAGE_ITEM_KV_DELIMITER = ",";

    public static String IDENTIFIERI_DELIMITER = ">";

    public static HeaderCarrier parse(HiggsRequestVO requestVO) {
        String traceIdField = HiggsPropagateHeaderEnum.TRACE_ID.getField();
        String traceId = requestVO.getTraceId();
        if (traceId == null || traceId.trim().equals("")) {
            return null;
        }

        HeaderCarrier headerCarrier = new HeaderCarrier();
        headerCarrier.put(traceIdField, traceId);
        headerCarrier.put(HiggsPropagateHeaderEnum.SPAN_ID.getField(), requestVO.getSpanId());
        headerCarrier.put(HiggsPropagateHeaderEnum.SPAN_INDEX.getField(), requestVO.getSpanIndex());
        headerCarrier.put(HiggsPropagateHeaderEnum.AGENT_TOKEN.getField(), requestVO.getAgentToken());
        headerCarrier.put(HiggsPropagateHeaderEnum.SPAN_REFERER.getField(), requestVO.getSpanReferer());
        headerCarrier.put(HiggsPropagateHeaderEnum.SPAN_BAGGAGE.getField(), requestVO.getSpanBaggage());
        return headerCarrier;
    }

    public String getHeaderCarrierIdentifier(String prefix) {
        StringBuilder identifierStr = new StringBuilder();
        if (prefix != null) {
            identifierStr.append(prefix);
        }
        for (HiggsPropagateHeaderEnum higgsPropagateHeaderEnum : HiggsPropagateHeaderEnum.getEnumSortArray()) {
            String value = textMap.get(higgsPropagateHeaderEnum.getField());
            if (value != null) {
                identifierStr.append(value);
            }
            identifierStr.append(IDENTIFIERI_DELIMITER);
        }
        return identifierStr.toString();
    }

    public static HeaderCarrier parseHeaderCarrierIdentifier(String prefix, String headerCarrierIdentifier) {
        String realHeaderCarrierIdentifier = headerCarrierIdentifier;
        if (headerCarrierIdentifier == null) {
            return null;
        }
        if (prefix != null) {
            if (!headerCarrierIdentifier.startsWith(prefix)) {
                return null;
            }
            realHeaderCarrierIdentifier = headerCarrierIdentifier.substring(prefix.length());

        }
        String[] identifierArray = realHeaderCarrierIdentifier.split(IDENTIFIERI_DELIMITER);
        if (identifierArray == null || identifierArray.length < 1) {
            return null;
        }
        HeaderCarrier headerCarrier = new HeaderCarrier();
        HiggsPropagateHeaderEnum[] higgsPropagateHeaderEnums = HiggsPropagateHeaderEnum.getEnumSortArray();
        for (int i = 0; i < identifierArray.length; i++) {
            if (i < higgsPropagateHeaderEnums.length) {
                headerCarrier.put(higgsPropagateHeaderEnums[i].getField(), identifierArray[i]);
            }

        }
        return headerCarrier;
    }

    public void setAgentToken(String agentToken) {
        textMap.put(HiggsPropagateHeaderEnum.AGENT_TOKEN.getField(), agentToken);
    }

    public String getAgentToken() {
        return textMap.get(HiggsPropagateHeaderEnum.AGENT_TOKEN.getField());
    }

    public void setTraceId(String traceId) {
        textMap.put(HiggsPropagateHeaderEnum.TRACE_ID.getField(), traceId);
    }

    public String getTraceId() {
        return textMap.get(HiggsPropagateHeaderEnum.TRACE_ID.getField());
    }

    public void setSpanId(String spanId) {
        textMap.put(HiggsPropagateHeaderEnum.SPAN_ID.getField(), spanId);
    }

    public String getSpanId() {
        return textMap.get(HiggsPropagateHeaderEnum.SPAN_ID.getField());
    }

    public void setSpanIndex(String spanIndex) {
        textMap.put(HiggsPropagateHeaderEnum.SPAN_INDEX.getField(), spanIndex);
    }

    public Integer getSpanIndex() {
        return Integer.valueOf(textMap.get(HiggsPropagateHeaderEnum.SPAN_INDEX.getField()));
    }

    public void setSpanReferer(String spanReferer) {
        textMap.put(HiggsPropagateHeaderEnum.SPAN_REFERER.getField(), spanReferer);
    }

    public void setSpanBaggage(Iterable<Map.Entry<String, String>> baggageItems) {
        if (baggageItems == null) {
            return;
        }
        StringBuilder spanBaggage = new StringBuilder();
        for (Map.Entry<String, String> baggageEntry : baggageItems) {
            spanBaggage.append(baggageEntry.getKey()).append(HeaderCarrier.BAGGAGE_ITEM_KV_DELIMITER)
                    .append(baggageEntry.getValue()).append(HeaderCarrier.BAGGAGE_ITEM_DELIMITER);

        }
        textMap.put(HiggsPropagateHeaderEnum.SPAN_BAGGAGE.getField(), spanBaggage.toString());
    }

    public void setSpanBaggage(String key, String value) {
        if (key == null || key.startsWith(HiggsPropagateHeaderEnum.HEADER_PREFIX)) {
            return;
        }
        String existsBaggage = textMap.get(HiggsPropagateHeaderEnum.SPAN_BAGGAGE.getField());
        StringBuilder spanBaggage;
        if (existsBaggage == null) {
            spanBaggage = new StringBuilder();
        } else {
            spanBaggage = new StringBuilder(
                    existsBaggage.length() + key.length() + value.length() + 2);
        }

        spanBaggage.append(key).append(HeaderCarrier.BAGGAGE_ITEM_KV_DELIMITER)
                .append(value).append(HeaderCarrier.BAGGAGE_ITEM_DELIMITER);
        textMap.put(HiggsPropagateHeaderEnum.SPAN_BAGGAGE.getField(), spanBaggage.toString());
    }

    public String getSpanReferer() {
        return textMap.get(HiggsPropagateHeaderEnum.SPAN_REFERER.getField());
    }

    public String getSpanBaggage(String key) {
        return getSpanBaggage().get(key);
    }

    public Map<String, String> getSpanBaggage() {
        String spanBaggage = textMap.get(HiggsPropagateHeaderEnum.SPAN_BAGGAGE.getField());
        if (spanBaggage == null || spanBaggage.trim().equals("")) {
            return Collections.EMPTY_MAP;
        }
        String[] baggageItems = spanBaggage.split(BAGGAGE_ITEM_DELIMITER);
        if (baggageItems == null || baggageItems.length < 1) {
            return Collections.EMPTY_MAP;
        }
        Map<String, String> baggageMap = new HashMap<String, String>(baggageItems.length);
        for (String baggageItem : baggageItems) {
            if (baggageItem == null || baggageItem.trim().equals("")) {
                continue;
            }
            int itemIndex = baggageItem.indexOf(BAGGAGE_ITEM_KV_DELIMITER);
            if (itemIndex == -1) {
                continue;
            }
            String itemKey = baggageItem.substring(0, itemIndex);
            String itemValue = baggageItem.substring(itemIndex + 1);
            baggageMap.put(itemKey, itemValue);
        }
        return baggageMap;
    }
}
