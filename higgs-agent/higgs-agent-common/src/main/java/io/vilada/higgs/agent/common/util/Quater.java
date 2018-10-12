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

public class Quater<T, E, N, G> {
    public Quater(T key, E value1, N value2, G value3) {
        this.key = key;
        this.value1 = value1;
        this.value2 = value2;
        this.value3 = value3;
    }

    public T getKey() {
        return key;
    }

    public E getValue1() {
        return value1;
    }

    public N getValue2() {
        return value2;
    }

    public G getValue3() { return value3; }

    private T key;
    private E value1;
    private N value2;
    private G value3;
}
