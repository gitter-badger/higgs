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

package io.vilada.higgs.agent.common.util;

import io.vilada.higgs.agent.common.HiggsAgentCommonExcepiton;
import io.vilada.higgs.agent.common.logging.HiggsAgentLogger;
import io.vilada.higgs.agent.common.logging.HiggsAgentLoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author mjolnir
 */
public class DigestUtils {

    private static HiggsAgentLogger LOGGER = HiggsAgentLoggerFactory.getLogger(DigestUtils.class);

    private static final String MD5_ALGORITHM_NAME = "MD5";

    private static final char[] HEX_CHARS =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * Return a hexadecimal string representation of the MD5 digest of the given bytes.
     * @param sourceStr the string to calculate the digest over
     * @return a hexadecimal digest string
     */
    public static String getMd5DigestHex(String sourceStr) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(MD5_ALGORITHM_NAME);
            byte[] digestBytes = messageDigest.digest(sourceStr.getBytes());
            char chars[] = new char[32];
            for (int i = 0; i < chars.length; i = i + 2) {
                byte b = digestBytes[i / 2];
                chars[i] = HEX_CHARS[(b >>> 0x4) & 0xf];
                chars[i + 1] = HEX_CHARS[b & 0xf];
            }
            return new String(chars);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("md5DigestAsHex occur error", e);
            throw new HiggsAgentCommonExcepiton("Get md5 str occur error");
        }
    }

}
