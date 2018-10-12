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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

/**
 * @author yawei
 * @created on 2017-6-15.
 */

/**
 *
 30m： 横轴以2分钟为单位，采集频率以1分钟为单位
 1小时：横轴以5分钟为单位，采集频率以2分钟为单位
 6小时：横轴以半小时为单位，采集频率以15分钟为单位
 12小时：横轴以1小时为单位，采集频率以30分钟为单位
 1天：横轴以2小时为单位，采集频率以1小时为单位
 3天：横轴以6小时为单位，采集频率以3小时为单位
 7天：横轴以12小时为单位，采集频率以6小时为单位
 14天：横轴以24小时为单位，采集频率以12小时为单位
 1月：横轴以48小时为单位，采集频率以24小时为单位
 */
public enum IntervalEnum {
    TIME_30M("30m","30分钟","1m"),
    TIME_1H("1h","1小时","2m"),
    TIME_6H("6h","6小时","15m"),
    TIME_12H("12h","12小时", "30m"),
    TIME_1D("1d","1天","1h"),
    TIME_3D("3d","3天","3h"),
    TIME_7D("7d","7天", "6h"),
    TIME_14D("14d","14天","12h"),
    TIME_1M("1M","1月","24h");

    @Getter
    private String key;

    @Getter
    private String name;

    @Getter
    private String interval;

    private static Map<String, IntervalEnum> timeMap = new HashMap<>();

    private static Map<String ,Integer> calendarUnitMap = new HashMap<>();

    static {
        for (IntervalEnum timeIntervalEnum : IntervalEnum.values()) {
            timeMap.put(timeIntervalEnum.key, timeIntervalEnum);
            calendarUnitMap.put("m", Calendar.MINUTE);
            calendarUnitMap.put("h", Calendar.HOUR);
            calendarUnitMap.put("d", Calendar.DATE);
            calendarUnitMap.put("M", Calendar.MONTH);

        }
    }

    IntervalEnum(String key, String name, String interval) {
        this.key = key;
        this.name = name;
        this.interval = interval;
    }

    public static IntervalEnum getInterval(String key) {
        return timeMap.get(key);
    }

}
