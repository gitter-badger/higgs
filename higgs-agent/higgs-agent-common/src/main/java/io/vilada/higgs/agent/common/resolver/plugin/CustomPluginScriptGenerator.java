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

import com.google.gson.Gson;

import java.util.*;

public class CustomPluginScriptGenerator {
    private static final String DEFAULT_JSON = "{}";
    private static final String DEFAULT_BUNDLE_NAME = "custom plugin transformer bundle";
    private static final String DEFAULT_INTERCEPTOR_NAME = "io.vilada.higgs.plugin.custom.interceptor.CustomInterceptor";

    /**
     * 输入参数是一个list，每个元素是拥有三个元素的数组，第一个元素代表类名，第二个代表方法名，第三个代表方法描述符
     * 例如list中一个元素可以是：[io.vilada.higgs.biz.Controller, doBusiness, (ILjava/lang/String;)Ljava/lang/Object;]
     * 整个List可以是：
     * {
     *     [io.vilada.higgs.biz.Controller, doBusiness, (ILjava/lang/String;)Ljava/lang/Object;],
     *     [io.vilada.higgs.biz.Controller, login, (Ljava/lang/String)V],
     *     [io.vilada.higgs.db.Query, getUser, (Ljava/lang/String)Ljava/lang/String;]
     * }
     * list中的数组可以重复，generate函数会自动去重，单独的类名，方法名，签名也可以重复，generate会处理
     *
     * @param propJson
     * @return
     */
    public static String generate(String propJson) {
        Gson gson = new Gson();
        List<Map<String, Object>> rawUnits = new ArrayList<Map<String, Object>>();
        rawUnits = gson.fromJson(propJson, rawUnits.getClass());

        if (rawUnits == null || rawUnits.isEmpty()) {
            return DEFAULT_JSON;
        }

        Map<ClassUnit, Set<MethodUnit>> classMethodMap = new HashMap<ClassUnit, Set<MethodUnit>>();
        for (Map<String, Object> bundle : rawUnits) {
            ClassUnit classUnit = new ClassUnit(bundle.get("className").toString(), Boolean.valueOf(bundle.get("findSubClass").toString()));
            Set<MethodUnit> methodInfoSet = classMethodMap.get(classUnit);
            if (methodInfoSet == null) {
                methodInfoSet = new HashSet<MethodUnit>();
                classMethodMap.put(classUnit, methodInfoSet);
            }
            MethodUnit methodUnit = new MethodUnit(bundle.get("methodName").toString(), bundle.get("description").toString());
            methodInfoSet.add(methodUnit);
        }

        PluginBundleInfo pluginBundleInfo = new PluginBundleInfo();
        pluginBundleInfo.setName(DEFAULT_BUNDLE_NAME);

        TransformerInfo[] transformers = new TransformerInfo[classMethodMap.size()];
        int i = 0;
        for (ClassUnit classUnit : classMethodMap.keySet()) {
            TargetInfo targetInfo = new TargetInfo();
            targetInfo.setClassName(classUnit.className);
            targetInfo.setFindSubClass(classUnit.findSubClass);
            targetInfo.setSubClassScopes(null);
            TargetInfo[] targetInfos = new TargetInfo[] {targetInfo};

            Set<MethodUnit> methodSet = classMethodMap.get(classUnit);
            MethodInfo[] methodInfos = new MethodInfo[methodSet.size()];
            int j = 0;
            for (Iterator<MethodUnit> itr = methodSet.iterator(); itr.hasNext();) {
                MethodUnit methodUnit = itr.next();
                MethodInfo methodInfo = new MethodInfo();
                methodInfo.setName(methodUnit.methodName);
                methodInfo.setDescriptor(methodUnit.description);
                methodInfos[j++] = methodInfo;
            }

            InterceptorInfo interceptorInfo = new InterceptorInfo();
            interceptorInfo.setClassName(DEFAULT_INTERCEPTOR_NAME);
            interceptorInfo.setConstructAgrs(null);

            InterceptorBundleInfo interceptorBundleInfo = new InterceptorBundleInfo();
            interceptorBundleInfo.setInterceptorInfo(interceptorInfo);
            interceptorBundleInfo.setMethods(methodInfos);
            InterceptorBundleInfo[] interceptorBundleInfos = new InterceptorBundleInfo[] {interceptorBundleInfo};


            TransformerInfo transformerInfo = new TransformerInfo();
            transformerInfo.setTargets(targetInfos);
            transformerInfo.setInterceptors(interceptorBundleInfos);
            transformerInfo.setFields(null);
            transformerInfo.setGetters(null);
            transformers[i++] = transformerInfo;
        }
        pluginBundleInfo.setTransformers(transformers);

        return PluginScriptResolver.serialize(pluginBundleInfo);
    }

    private static class ClassUnit {
        private String className;
        private boolean findSubClass;

        ClassUnit(String className, boolean findSubClass) {
            this.className = className;
            this.findSubClass = findSubClass;
        }

        public boolean equals(Object other) {
            if (other == null || !(other instanceof ClassUnit)) {
                return false;
            } else {
                ClassUnit otherClassUnit = (ClassUnit)other;
                return otherClassUnit.className.equals(this.className);
            }
        }

        public int hashCode() {
            return className.hashCode();
        }
    }

    private static class MethodUnit {
        private String methodName;
        private String description;

        MethodUnit(String methodName, String description) {
            this.methodName = methodName;
            this.description = description;
        }

        public boolean equals(Object other) {
            if (other == null || !(other instanceof MethodUnit)) {
                return false;
            } else {
                MethodUnit otherMethodUnit = (MethodUnit)other;
                return otherMethodUnit.methodName.equals(this.methodName) && otherMethodUnit.description.equals(this.description);
            }
        }

        public int hashCode() {
            return (methodName.hashCode() + description.hashCode())/2;
        }
    }
}
