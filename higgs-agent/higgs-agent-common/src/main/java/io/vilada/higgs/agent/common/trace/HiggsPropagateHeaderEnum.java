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

import java.util.HashMap;
import java.util.Map;

/**
 * @author mjolnir
 */
public enum HiggsPropagateHeaderEnum {
    
    AGENT_TOKEN("Higgs-AgentToken", 1),
    TRACE_ID("Higgs-TraceId", 2),
    SPAN_ID("Higgs-SpanId", 3),
    SPAN_INDEX("Higgs-SpanIndex", 4),
    SPAN_BAGGAGE("Higgs-SpanBaggage", 5),
    SPAN_REFERER("Higgs-SpanReferer", 6),
    SPAN_BAGGAGE_X_HTTP_TARGET("HX-HT", 7); // for custom
    
    public static String HEADER_PREFIX = "Higgs-";

    private String field;

    private int id;

    HiggsPropagateHeaderEnum(String field, int id) {
        this.field = field;
        this.id = id;
    }

    public String getField() {
        return field;
    }

    public int getId() {
        return id;
    }

    private static final Map<String, HiggsPropagateHeaderEnum> NAME_SET = new HashMap<String, HiggsPropagateHeaderEnum>();

    private static final Map<Integer, HiggsPropagateHeaderEnum> ID_SET = new HashMap<Integer, HiggsPropagateHeaderEnum>();

    private static final HiggsPropagateHeaderEnum[] SORT_HEADER_ARRAY = {AGENT_TOKEN, TRACE_ID,
            SPAN_ID, SPAN_INDEX, SPAN_BAGGAGE, SPAN_REFERER, SPAN_BAGGAGE_X_HTTP_TARGET};

    static {
        for (HiggsPropagateHeaderEnum higgsPropagateHeaderEnum : HiggsPropagateHeaderEnum.values()) {
            NAME_SET.put(higgsPropagateHeaderEnum.field, higgsPropagateHeaderEnum);
            ID_SET.put(higgsPropagateHeaderEnum.id, higgsPropagateHeaderEnum);
        }
    }

    public static HiggsPropagateHeaderEnum getHeader(String name) {
        if (name == null) {
            return null;
        }
        if (!name.startsWith(HEADER_PREFIX)) {
            return null;
        }
        return NAME_SET.get(name);
    }

    public static HiggsPropagateHeaderEnum getHeader(int id) {
        return ID_SET.get(id);
    }

    public static HiggsPropagateHeaderEnum[] getEnumSortArray() {
        return SORT_HEADER_ARRAY;
    }

}
