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

package io.vilada.higgs.agent.engine.util;

public class XORShiftRandom {
    private static volatile long state = 0xCAFEBABE; // initial non-zero value

    public static final long nextLong() {
        long a=state;
        state = xorShift64(a);
        return a;
    }

    public static final long xorShift64(long a) {
        a ^= (a << 21);
        a ^= (a >>> 35);
        a ^= (a << 4);
        return a;
    }

    public static final int random(int n) {
        if (n<0) throw new IllegalArgumentException();
        long result=((nextLong()>>>32)*n)>>32;
        return (int) result;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i ++) {
            int value = XORShiftRandom.random(10);
            System.out.println(value);
        }
    }
}
