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
 * Created by yawei on 2017-7-10.
 */
public enum JvmGCArea {
    NEW(0),
    OLD(1),
    UNKNOWN(2);

    @Getter
    private final int value;

    JvmGCArea(int value) {
        this.value = value;
    }

    /**
     * Find a the enum type by its integer value, as defined in the Thrift IDL.
     * @return null if the value is not found.
     */
    public static JvmGCArea findByValue(int value) {
        switch (value) {
            case 0:
                return NEW;
            case 1:
                return OLD;
            case 2:
                return UNKNOWN;
            default:
                return null;
        }
    }
}
