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

package io.vilada.higgs.data.meta.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-07-03 下午4:47
 **/

public class SystemConfigAlreadyExistException extends RuntimeException {

    @Setter
    @Getter
    private String systeConfigName;

    public SystemConfigAlreadyExistException() {
        super();
    }

    public SystemConfigAlreadyExistException(String message) {
        super(message);
    }

    public SystemConfigAlreadyExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public SystemConfigAlreadyExistException(Throwable cause) {
        super(cause);
    }
}


