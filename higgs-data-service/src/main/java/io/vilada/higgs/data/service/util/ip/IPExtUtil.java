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

package io.vilada.higgs.data.service.util.ip;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public class IPExtUtil {

    private static final Pattern IPV4_PATTERN =
            Pattern.compile("^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");

    public static boolean isIPv4Address(final String input) {
        return IPV4_PATTERN.matcher(input).matches();
    }

    public static IPInfo getInfo(String ip) {

        if (StringUtils.isEmpty(ip)) {
            return null;
        }
        String[] strings = IPExt.find(ip);
        if (strings == null || strings.length != 13) {
            return null;
        }
        IPInfo ipInfo = IPInfo.builder().country(strings[0])
                .province(strings[1])
                .city(strings[2])
                .school(strings[3])
                .operator(strings[4])
                .latitude(strings[5])
                .longitude(strings[6])
                .timezone1(strings[7])
                .timezone2(strings[8])
                .administrativeAreaCode(strings[9])
                .internationalPhoneCode(strings[10])
                .countryTwoDigitCode(strings[11])
                .worldContinentCode(strings[12]).build();

        return ipInfo;
    }

}