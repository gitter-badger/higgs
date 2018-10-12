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

package io.vilada.higgs.data.web.service.elasticsearch.service.v2.database.topn;

import java.util.Collection;

public class ValidatorFactory {
    private static final Validator<String> nonEmptyStringValidator = new Validator<String>() {
        @Override
        public boolean validate(String value) {
            return value == null ? false : !value.trim().isEmpty();
        }
    };

    private static final Validator<Collection> nonEmptyCollectionValidator = new Validator<Collection>() {
        @Override
        public boolean validate(Collection value) {
            return value == null ? false : !value.isEmpty();
        }
    };

    private static final Validator<Number> positiveNumberValidator = new Validator<Number>() {
        @Override
        public boolean validate(Number value) {
            return value == null ? false : (value.doubleValue() < 0 ? false : true);
        }
    };

    public static Validator<String> getNonEmptyStringValidator() {
        return nonEmptyStringValidator;
    }

    public static Validator<Collection> getNonEmptyCollectionValidator() {
        return nonEmptyCollectionValidator;
    }

    public static Validator<Number> getPositiveNumberValidator() {
        return positiveNumberValidator;
    }
}
