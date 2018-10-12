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

package io.vilada.higgs.common.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ethan
 */
public class HiggsThreadFactory implements ThreadFactory {

    private final static AtomicInteger FACTORY_NUMBER = new AtomicInteger(0);

    public static String THREAD_PREFIX = "Higgs-";

    public final String threadPrefix;

    private final boolean daemon;

    public HiggsThreadFactory(String threadName) {
        this(threadName, false);
    }

    public HiggsThreadFactory(String threadName, boolean daemon) {
        if (threadName == null) {
            throw new NullPointerException("threadName");
        }
        this.threadPrefix = prefix(threadName);
        this.daemon = daemon;
    }

    private String prefix(String threadName) {
        final StringBuilder buffer = new StringBuilder(32);
        buffer.append(THREAD_PREFIX)
                .append(threadName)
                .append('-');
        return buffer.toString();
    }


    public Thread newThread(Runnable job) {
        StringBuilder threadName = new StringBuilder(threadPrefix.length() + 8);
        threadName.append(threadPrefix);
        threadName.append(FACTORY_NUMBER.getAndIncrement());
        Thread thread = new Thread(job, threadName.toString());
        if (daemon) {
            thread.setDaemon(true);
        }
        return thread;
    }


    public static ThreadFactory createThreadFactory(String threadName) {
        return new HiggsThreadFactory(threadName, false);
    }

}
