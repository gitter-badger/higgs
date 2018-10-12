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

package io.vilada.higgs.agent.common.resolver.plugin;

public class TargetInfo {
    private String className;
    private boolean findSubClass;
    private String[] subClassScopes;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public boolean isFindSubClass() {
        return findSubClass;
    }

    public void setFindSubClass(boolean findSubClass) {
        this.findSubClass = findSubClass;
    }

    public String[] getSubClassScopes() {
        return subClassScopes;
    }

    public void setSubClassScopes(String[] subClassScopes) {
        this.subClassScopes = subClassScopes;
    }
}
