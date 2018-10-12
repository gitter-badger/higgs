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

import io.vilada.higgs.agent.common.plugin.ObjectFactory;
import io.vilada.higgs.agent.engine.util.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;

/**
 * @author ethan
 */
public class OrderedValueProvider implements JudgingParameterResolver {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AutoBindingObjectFactory objectFactory;
    private final Object[] values;
    private int index = 0;

    public OrderedValueProvider(AutoBindingObjectFactory objectFactory, Object[] values) {
        this.objectFactory = objectFactory;
        this.values = values;
    }


    public void prepare() {
        index = -1;
        prepareNextCandidate();
    }


    public Option get(int index, Class<?> type, Annotation[] annotations) {
        if (this.index >= values.length) {
            return Option.empty();
        }
        
        final Object value = values[this.index];
        
        if (type.isPrimitive()) {
            if (value == null) {
                return Option.empty();
            }
            
            if (TypeUtils.getWrapperOf(type) == value.getClass()) {
                prepareNextCandidate();
                return Option.withValue(value); 
            }
        } else {
            if (type.isInstance(value)) {
                prepareNextCandidate();
                return Option.withValue(value);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("argument miss match index:{}, type:{} value:{} typeCl:{}, valueCl:{}", this.index, type, value, type.getClassLoader(), getClassLoader(value));
                }
            }
        }
        
        return Option.empty();
    }

    private ClassLoader getClassLoader(Object object) {
        if (object == null) {
            return null;
        }
        return object.getClass().getClassLoader();
    }

    private void prepareNextCandidate() {
        index++;
        
        if (index >= values.length) {
            return;
        }
        
        Object val = values[index];
        
        if (val instanceof ObjectFactory) {
            val = objectFactory.createInstance((ObjectFactory)val);
            values[index] = val;
        }
    }


    public boolean isAcceptable() {
        return index == values.length;
    }
}
