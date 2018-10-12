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

package io.vilada.higgs.data.service.enums;


import lombok.Getter;

public enum SingleTransHealthStatusEnum {
    //min, max is time / T(Apdex time).s
    NORMAL(0, 1),
    SLOW(1, 4),
    VERY_SLOW(4, Double.MAX_VALUE),
    ERROR(Double.MIN_VALUE, 0);

    @Getter
    private double minApdexTimeRate;

    @Getter
    private double maxApdexTimeRate;

    SingleTransHealthStatusEnum(double minApdexTimeRate, double maxApdexTimeRate ) {
        this.minApdexTimeRate = minApdexTimeRate;
        this.maxApdexTimeRate = maxApdexTimeRate;
    }

    public static boolean validate(String transType) {
        for (SingleTransHealthStatusEnum statusEnum: values()) {
            if (statusEnum.name().equals(transType)) {
                return true;
            }
        }

        return false;
    }

    public static SingleTransHealthStatusEnum getTransStatus(double apdexTimeRate) {
        if (apdexTimeRate < 0) {
            throw new IllegalArgumentException();
        }

        for (SingleTransHealthStatusEnum statusEnum: values()) {
            if (statusEnum.matched(apdexTimeRate)) {
                return statusEnum;
            }
        }

        return null;
    }

    private boolean matched(double apdexTimeRate) {
        return apdexTimeRate >= minApdexTimeRate && apdexTimeRate < maxApdexTimeRate;
    }
}
