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

package io.vilada.higgs.data.web.service.elasticsearch.index.agentstat;

import lombok.Getter;

/**
 * Created by Administrator on 2017-5-31.
 */
public enum  JvmGcType {
    UNKNOWN(0, JvmGCArea.UNKNOWN, "unknown"),
    SERIAL_NEW(1, JvmGCArea.NEW, "copy"),
    SERIAL_OLD(2, JvmGCArea.OLD, "marksweepcompact"),
    PARNEW_NEW(3, JvmGCArea.NEW, "parnew"),
    PARALLEL_SCAVENGE_NEW(4, JvmGCArea.NEW, "ps-scavenge"),
    PARALLEL_SCAVENGE_OLD(5, JvmGCArea.OLD, "ps-marksweep"),
    CMS_OLD(6, JvmGCArea.OLD, "concurrentmarksweep"),
    G1_NEW(7, JvmGCArea.NEW, "g1-young"),
    G1_OLD(8, JvmGCArea.OLD, "g1-mixed"),
    UNKNOWN_NEW(9, JvmGCArea.NEW, "young"),
    UNKNOWN_OLD(10, JvmGCArea.OLD, "old");

    @Getter
    private final int value;
    @Getter
    private JvmGCArea area;
    @Getter
    private String algorithm;

    JvmGcType(int value, JvmGCArea area, String algorithm) {
        this.value = value;
        this.area = area;
        this.algorithm = algorithm;
    }

    /**
     * Find a the enum type by its integer value, as defined in the Thrift IDL.
     * @return null if the value is not found.
     */
    public static JvmGcType findByValue(int value) {
        switch (value) {
            case 0:
                return UNKNOWN;
            case 1:
                return SERIAL_NEW;
            case 2:
                return SERIAL_OLD;
            case 3:
                return PARNEW_NEW;
            case 4:
                return PARALLEL_SCAVENGE_NEW;
            case 5:
                return PARALLEL_SCAVENGE_OLD;
            case 6:
                return CMS_OLD;
            case 7:
                return G1_NEW;
            case 8:
                return G1_OLD;
            default:
                return UNKNOWN;
        }
    }
}
