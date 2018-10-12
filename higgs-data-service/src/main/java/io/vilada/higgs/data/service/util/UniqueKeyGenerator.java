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

package io.vilada.higgs.data.service.util;

import com.sohu.idcenter.IdWorker;
import org.apache.commons.lang.RandomStringUtils;

import java.util.UUID;

/**
 * unique key generator
 *
 * @author nianjun
 * @create 2017-06-29 下午3:57
 **/
public class UniqueKeyGenerator {

    // TODO make it distribution by nianjun
    private static final IdWorker ID_WORKER = new IdWorker(1);

    /**
     * generate unique random long number by uuid
     *
     * @return
     */
    public static Long generateLong() {
        return UUID.randomUUID().getLeastSignificantBits();
    }

    /**
     * generate UUID str
     *
     * @return
     */
    public static String generateStr() {
        return UUID.randomUUID().toString();
    }

    /**
     * generate unique long type number
     *
     * @return
     */
    public static String generate() {
        return generateStr();
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

    public static Long generateSnowFlakeId() {
        return UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
    }
}
