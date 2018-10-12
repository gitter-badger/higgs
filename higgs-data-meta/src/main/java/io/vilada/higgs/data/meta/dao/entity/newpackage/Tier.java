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

package io.vilada.higgs.data.meta.dao.entity.newpackage;

import lombok.Data;

import java.util.Date;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-08-24 下午8:11
 **/

@Data
public class Tier {

    /**
     * tier id
     */
    private Long id;

    /**
     * tier name
     */
    private String name;

    private Byte tierType;

    /**
     * tier description
     */
    private String description;

    /**
     * create time
     */
    private Date createTime;

    /**
     * update time
     */
    private Date updateTime;

}
