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

package io.vilada.higgs.agent.engine.trace;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ethan
 */
public class HiggsIdGenerator {

    public static String DELIMITER = "-";

    private static long START_MILLISECONDS = 1490976000000L;

    private static String PROCESS_ID;

    private static final ThreadLocal<AtomicInteger> THREAD_ID_SEQUENCE = new ThreadLocal<AtomicInteger>() {
        @Override
        protected AtomicInteger initialValue() {
            return new AtomicInteger();
        }
    };

    public static void init(String agentIdentifier) {
        PROCESS_ID = agentIdentifier.replace(DELIMITER, "");
    }

    /**
     * generate unique id for trace.
     *
     * max length 64bytes
     *
     * [32bytes - unique node+process id][8byes - thread id][16bytes - times][8bytes - sequence]
     *
     * @see <a href="http://github.com/twitter/snowflake">twitter/snowflake</a>
     *
     * @return
     */
    public static String generateId() {
        if (PROCESS_ID == null) {
            return null;
        }
        int seq = THREAD_ID_SEQUENCE.get().incrementAndGet();
        int threadId = Long.valueOf(Thread.currentThread().getId()).intValue();
        StringBuilder sb = new StringBuilder(68)
                .append(PROCESS_ID).append(DELIMITER)
                .append(Integer.toHexString(threadId)).append(DELIMITER)
                .append(Long.toHexString(System.currentTimeMillis() - START_MILLISECONDS)).append(DELIMITER)
                .append(Integer.toHexString(seq));
        return sb.toString();
    }

}
