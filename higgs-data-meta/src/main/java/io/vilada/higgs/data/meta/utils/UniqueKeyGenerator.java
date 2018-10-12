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

package io.vilada.higgs.data.meta.utils;

import com.sohu.idcenter.IdWorker;
import org.apache.commons.lang.RandomStringUtils;

/**
 *
 * @author nianjun
 * @create 2017-06-29 下午3:57
 **/
public class UniqueKeyGenerator {

    private static final IdWorker ID_WORKER = new IdWorker();

    public static Long getSnowFlakeId(){
        return ID_WORKER.getId();
    }

    public static String genreateToken(int size) {
        if (size < 1) {
            size = 1;
        }

        return RandomStringUtils.randomAlphanumeric(size);
    }

    public static String genrateToken() {
        return RandomStringUtils.randomAlphanumeric(8);
    }
}
