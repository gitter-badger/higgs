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

package io.vilada.higgs.agent.engine.instrument.objectfactory;

public abstract class Option {
    public abstract Object getValue();
    public abstract boolean hasValue();
    
    public static Option withValue(Object value) {
        return new WithValue(value);
    }
    
    @SuppressWarnings("unchecked")
    public static Option empty() {
        return (Option)EMPTY;
    }
    
    private static final class WithValue extends Option {
        private final Object value;
        
        private WithValue(Object value) {
            this.value = value;
        }


        public Object getValue() {
            return value;
        }


        public boolean hasValue() {
            return true;
        }
        
    }
 
    private static final Option EMPTY = new Option() {


        public Object getValue() {
            return null;
        }


        public boolean hasValue() {
            return false;
        }
    };
    
}