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

package io.vilada.higgs.agent.engine.trace.opentracing;

import java.util.HashMap;
import java.util.Map;

public class HiggsTraceData<E, T> {
    private final Map<E, T> map = new HashMap<E, T>();

    public void put(E key, T value) {
        map.put(key, value);
    }

    public T putIfAbsent(E key, T value) {
        T old = map.get(key);
        if (old == null) {
            map.put(key, value);
        }
        return old;
    }

    public T get(E key) {
        return map.get(key);
    }

    public T getAndRemove(E key) {
        T old = map.get(key);
        map.remove(key);
        return old;
    }
}
