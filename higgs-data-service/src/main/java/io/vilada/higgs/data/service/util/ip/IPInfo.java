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

import lombok.Builder;
import lombok.Data;

/**
 * Created by lihaiguang on 2017/12/8.
 */
@Builder
@Data
public class IPInfo {
    /**
     * 国家
    */
    private String country;

    /**
     * 省会或直辖市（国内）
    */
    private String province;

    /**
     * 地区或城市(国内)
     */
    private String city;

    /**
     * 学校或单位 （国内）
     */
    private String school;

    /**
     * 运营商字段（只有购买了带有运营商版本的数据库才会有）
     * */
    private String operator;

    /**
     * 纬度
     */
    private String latitude;

    /**
     * 经度
     */
    private String longitude;

    /**
     * 时区一, 可能不存在
     */
    private String timezone1;

    /**
     * 时区二, 可能不存在
     */
    private String timezone2;

    /**
     * 中国行政区划代码
     */
    private String administrativeAreaCode;

    /**
     * 国际电话代码
     */
    private String internationalPhoneCode;

    /**
     * 国家二位代码
     */
    private String countryTwoDigitCode;

    /**
     * 世界大洲代码
     */
    private String worldContinentCode;

}
