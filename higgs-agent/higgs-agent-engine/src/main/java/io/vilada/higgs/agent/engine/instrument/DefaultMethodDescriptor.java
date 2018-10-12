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

package io.vilada.higgs.agent.engine.instrument;

import io.vilada.higgs.agent.common.instrument.MethodDescriptor;
import io.vilada.higgs.agent.engine.util.ApiUtils;

import java.util.Arrays;

/**
 * @author mjolnir
 */
public class DefaultMethodDescriptor implements MethodDescriptor {

    private String className;

    private String methodName;

    private String[] parameterTypes;

    private String[] parameterVariableName;

    private String parameterDescriptor;

    private String apiDescriptor;

    private int lineNumber;

    private int apiId = 0;

    private String fullName;
    
    private int type = 0;

    private String classAndMethodName;

    public DefaultMethodDescriptor(String className, String methodName,
        String[] parameterTypes, String[] parameterVariableName, int lineNumber) {
        this.className = className;
        this.methodName = methodName;
        this.classAndMethodName = new StringBuilder(128)
                .append(className).append(".").append(methodName).toString();
        this.parameterTypes = parameterTypes;
        this.parameterVariableName = parameterVariableName;
        this.parameterDescriptor = ApiUtils.mergeParameterVariableNameDescription(parameterTypes, parameterVariableName);
        this.apiDescriptor = ApiUtils.mergeApiDescriptor(className, methodName, parameterDescriptor);
        this.lineNumber = lineNumber;
    }

    public String getParameterDescriptor() {
        return parameterDescriptor;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getClassName() {
        return className;
    }


    public void setClassName(String className) {
        this.className = className;
    }


    public String[] getParameterTypes() {
        return parameterTypes;
    }


    public String[] getParameterVariableName() {
        return parameterVariableName;
    }


    public int getLineNumber() {
        return lineNumber;
    }


    public String getFullName() {
        if (fullName != null) {
            return fullName;
        }
        StringBuilder buffer = new StringBuilder(256);
        buffer.append(className);
        buffer.append(".");
        buffer.append(methodName);
        buffer.append(parameterDescriptor);
        if (lineNumber != -1) {
            buffer.append(":");
            buffer.append(lineNumber);
        }
        fullName = buffer.toString();
        return fullName;
    }

    public String getClassAndMethodName() {
        return classAndMethodName;
    }

    public String getApiDescriptor() {
        return apiDescriptor;
    }


    public void setApiId(int apiId) {
        this.apiId = apiId;
    }


    public int getApiId() {
        return apiId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{className=");
        builder.append(className);
        builder.append(", methodName=");
        builder.append(methodName);
        builder.append(", parameterTypes=");
        builder.append(Arrays.toString(parameterTypes));
        builder.append(", parameterVariableName=");
        builder.append(Arrays.toString(parameterVariableName));
        builder.append(", parameterDescriptor=");
        builder.append(parameterDescriptor);
        builder.append(", apiDescriptor=");
        builder.append(apiDescriptor);
        builder.append(", lineNumber=");
        builder.append(lineNumber);
        builder.append(", apiId=");
        builder.append(apiId);
        builder.append(", fullName=");
        builder.append(fullName);
        builder.append(", type=");
        builder.append(type);
        builder.append("}");
        return builder.toString();
    }
}
