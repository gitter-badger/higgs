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

package io.vilada.higgs.agent.common.config;

import java.util.HashSet;
import java.util.Set;

/**
 * @author mjolnir
 */
public class ProfilableClassFilter implements Filter<String> {

    private final Set<String> profileInclude = new HashSet<String>();
    private final Set<String> profileIncludeSub = new HashSet<String>();

    public ProfilableClassFilter(String profilableClass) {
        if (profilableClass == null || profilableClass.length() == 0) {
            return;
        }
        String[] className = profilableClass.split(",");
        for (String str : className) {
            if (str.endsWith(".*")) {
                this.profileIncludeSub.add(str.substring(0, str.length() - 2).replace('.', '/') + "/");
            } else {
                String replace = str.trim().replace('.', '/');
                this.profileInclude.add(replace);
            }
        }
    }

    /**
     * TODO remove this. Added this method to test the "call stack view" on a test server
     *
     * @param className
     * @return
     */

    public boolean filter(String className) {
        if (profileInclude.contains(className)) {
            return true;
        } else {
            final String packageName = className.substring(0, className.lastIndexOf("/") + 1);
            for (String pkg : profileIncludeSub) {
                if (packageName.startsWith(pkg)) {
                    return true;
                }
            }
        }
        return false;
    }



    public String toString() {
        final StringBuilder sb = new StringBuilder("ProfilableClassFilter{");
        sb.append("profileInclude=").append(profileInclude);
        sb.append(", profileIncludeSub=").append(profileIncludeSub);
        sb.append('}');
        return sb.toString();
    }
}
