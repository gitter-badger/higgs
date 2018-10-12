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

public class Pair<E, T> {
    E left;
    T right;
    public Pair(E left, T right) {
        this.left = left;
        this.right = right;
    }
    public E getLeft() {
        return left;
    }

    public T getRight() {
        return right;
    }

    public boolean equals(Object other) {
        if (!(other instanceof Pair)) {
            return false;
        }
        Pair<E, T> otherPair = (Pair<E, T>)other;
        return otherPair.left.equals(this.left) && otherPair.right.equals(this.right);
    }

    public int hashCode() {
        return this.left.hashCode() + this.right.hashCode();
    }
}
