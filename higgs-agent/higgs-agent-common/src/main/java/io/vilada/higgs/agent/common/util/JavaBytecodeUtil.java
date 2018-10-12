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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ethan
 */
public final class JavaBytecodeUtil {
    private static final String ARRAY = "[]";

    private static final Map<String, String> PRIMITIVE_JAVA_TO_JVM;
    private static final Map<String, String> JVM_TO_PRIMITIVE_JAVA;

    static {
        PRIMITIVE_JAVA_TO_JVM = new HashMap<String, String>();
        JVM_TO_PRIMITIVE_JAVA = new HashMap<String, String>();
        PRIMITIVE_JAVA_TO_JVM.put("byte", "B");
        JVM_TO_PRIMITIVE_JAVA.put("B", "byte");
        PRIMITIVE_JAVA_TO_JVM.put("char", "C");
        JVM_TO_PRIMITIVE_JAVA.put("C", "char");
        PRIMITIVE_JAVA_TO_JVM.put("double", "D");
        JVM_TO_PRIMITIVE_JAVA.put("D", "double");
        PRIMITIVE_JAVA_TO_JVM.put("float", "F");
        JVM_TO_PRIMITIVE_JAVA.put("F", "float");
        PRIMITIVE_JAVA_TO_JVM.put("int", "I");
        JVM_TO_PRIMITIVE_JAVA.put("I", "int");
        PRIMITIVE_JAVA_TO_JVM.put("long", "J");
        JVM_TO_PRIMITIVE_JAVA.put("J", "long");
        PRIMITIVE_JAVA_TO_JVM.put("short", "S");
        JVM_TO_PRIMITIVE_JAVA.put("S", "short");
        PRIMITIVE_JAVA_TO_JVM.put("void", "V");
        JVM_TO_PRIMITIVE_JAVA.put("V", "void");
        PRIMITIVE_JAVA_TO_JVM.put("boolean", "Z");
        JVM_TO_PRIMITIVE_JAVA.put("Z", "boolean");
    }

    private JavaBytecodeUtil() {
    }

    public static String javaTypeToJvmSignature(String[] javaTypeArray) {
        if (javaTypeArray == null || javaTypeArray.length == 0) {
            return "()";
        }
        final StringBuilder buffer = new StringBuilder();
        buffer.append('(');
        for (String javaType : javaTypeArray) {
            final String jvmSignature = toJvmSignature(javaType);
            buffer.append(jvmSignature);
        }
        buffer.append(')');
        return buffer.toString();

    }

    public static String javaTypeToJvmSignature(String[] paramTypes, String returnType) {
        final StringBuilder buffer = new StringBuilder();
        if (paramTypes == null || paramTypes.length == 0) {
            buffer.append("()");
        } else {
            buffer.append('(');
            for (String javaType : paramTypes) {
                final String jvmSignature = toJvmSignature(javaType);
                buffer.append(jvmSignature);
            }
            buffer.append(')');
        }
        if (returnType == null || returnType.trim().length() == 0) {
            final String jvmSignature = toJvmSignature(returnType);
            buffer.append(jvmSignature);
        } else {
            buffer.append("V");
        }
        return buffer.toString();
    }

    public static String toJvmSignature(Method method) {
        Class<?>[] parameters = method.getParameterTypes();
        StringBuilder buf = new StringBuilder();
        buf.append('(');
        for (int i = 0; i < parameters.length; ++i) {
            getDescriptor(buf, parameters[i]);
        }
        buf.append(')');
        getDescriptor(buf, method.getReturnType());
        return buf.toString();
    }

    public static String toJvmSignature(String javaType) {
        if (javaType == null) {
            throw new NullPointerException("javaType must not be null");
        }
        if (javaType.length() == 0) {
            throw new IllegalArgumentException("invalid javaType. \"\"");
        }

        final int javaObjectArraySize = getJavaObjectArraySize(javaType);
        final int javaArrayLength = javaObjectArraySize * 2;
        String pureJavaType;
        if (javaObjectArraySize != 0) {
            // pure java
            pureJavaType = javaType.substring(0, javaType.length() - javaArrayLength);
        } else {
            pureJavaType = javaType;
        }
        final String signature = PRIMITIVE_JAVA_TO_JVM.get(pureJavaType);
        if (signature != null) {
            // primitive type
            return appendJvmArray(signature, javaObjectArraySize);
        }
        return toJvmObject(javaObjectArraySize, pureJavaType);

    }

    private static String toJvmObject(int javaObjectArraySize, String pureJavaType) {
        final StringBuilder buffer = new StringBuilder(pureJavaType.length() + javaObjectArraySize + 2);
        for (int i = 0; i < javaObjectArraySize; i++) {
            buffer.append('[');
        }
        buffer.append('L');
        buffer.append(javaNameToJvmName(pureJavaType));
        buffer.append(';');
        return buffer.toString();
    }

    /**
     * java.lang.String -> java/lang/String
     * @param javaName
     * @return
     */
    public static String javaNameToJvmName(String javaName) {
        if (javaName == null) {
            throw new NullPointerException("javaName must not be null");
        }
        return javaName.replace('.', '/');
    }

    /**
     * java/lang/String -> java.lang.String
     * @param jvmName
     * @return
     */
    public static String jvmNameToJavaName(String jvmName) {
        if (jvmName == null) {
            throw new NullPointerException("jvmName must not be null");
        }
        String name = JVM_TO_PRIMITIVE_JAVA.get(jvmName);
        if (name != null) {
            return name;
        }
        jvmName = jvmName.replace('/', '.');
        if (jvmName.charAt(jvmName.length() - 1) != ';') {
            return jvmName;
        } else {
            int index = jvmName.indexOf('L');
            return jvmName.substring(index + 1, jvmName.length() - 1);
        }
    }

    /**
     * java/lang/String -> java.lang.String
     * @param jvmNameArray
     * @return
     */
    public static List<String> jvmNameToJavaName(String[] jvmNameArray) {
        if (jvmNameArray == null) {
            return Collections.emptyList();
        }
        List<String> list = new ArrayList<String>(jvmNameArray.length);
        for (String jvmName : jvmNameArray) {
            list.add(jvmNameToJavaName(jvmName));
        }
        return list;
    }

    private static String appendJvmArray(String signature, int javaObjectArraySize) {
        if (javaObjectArraySize == 0) {
            return signature;
        }
        StringBuilder sb = new StringBuilder(signature.length() + javaObjectArraySize);
        for (int i = 0; i < javaObjectArraySize; i++) {
            sb.append('[');
        }
        sb.append(signature);
        return sb.toString();
    }

    static int getJavaObjectArraySize(String javaType) {
        if (javaType == null) {
            throw new NullPointerException("javaType must not be null");
        }
        if (javaType.length() == 0) {
            return 0;
        }
        final int endIndex = javaType.length() - 1;
        final char checkEndArrayExist = javaType.charAt(endIndex);
        if (checkEndArrayExist != ']') {
            return 0;
        }
        int arraySize = 0;
        for (int i = endIndex; i > 0; i = i - 2) {
            final char arrayEnd = javaType.charAt(i);
            final char arrayStart = javaType.charAt(i - 1);
            if (arrayStart == '[' && arrayEnd == ']') {
                arraySize++;
            } else {
                return arraySize;
            }
        }
        return arraySize;
    }

    public static String javaClassNameToObjectName(String javaClassName) {
        final char scheme = javaClassName.charAt(0);
        switch (scheme) {
            case '[':
                return toArrayType(javaClassName);
            default:
                return javaClassName;
        }
    }

    // to variable name.
    // '.' '$' '[' ']' => '_'
    public static String javaClassNameToVariableName(String javaClassName) {
        if (javaClassName == null) {
            throw new NullPointerException("java class name must not be null");
        }

        return javaClassName.replace('.', '_')
                .replace('$', '_')
                .replace('[', '_')
                .replace(']', '_');
    }

    private static String byteCodeSignatureToObjectType(String signature, int startIndex) {
        final char scheme = signature.charAt(startIndex);
        switch (scheme) {
            case 'B':
                return "byte";
            case 'C':
                return "char";
            case 'D':
                return "double";
            case 'F':
                return "float";
            case 'I':
                return "int";
            case 'J':
                return "long";
            case 'S':
                return "short";
            case 'V':
                return "void";
            case 'Z':
                return "boolean";
            case 'L':
                return toObjectType(signature, startIndex + 1);
            case '[': {
                return toArrayType(signature);
            }
        }
        throw new IllegalArgumentException("invalid signature :" + signature);
    }

    private static String toArrayType(String description) {
        final int arraySize = getArraySize(description);
        final String objectType = byteCodeSignatureToObjectType(description, arraySize);
        return arrayType(objectType, arraySize);
    }

    private static String arrayType(String objectType, int arraySize) {
        final int arrayStringLength = ARRAY.length() * arraySize;
        StringBuilder sb = new StringBuilder(objectType.length() + arrayStringLength);
        sb.append(objectType);
        for (int i = 0; i < arraySize; i++) {
            sb.append(ARRAY);
        }
        return sb.toString();
    }

    private static int getArraySize(String description) {
        if (description == null || description.length() == 0) {
            return 0;
        }
        int arraySize = 0;
        for (int i = 0; i < description.length(); i++) {
            final char c = description.charAt(i);
            if (c == '[') {
                arraySize++;
            } else {
                break;
            }
        }
        return arraySize;
    }

    private static String toObjectType(String signature, int startIndex) {
        // Ljava/lang/String;
        final String assistClass = signature.substring(startIndex, signature.length() - 1);
        final String objectName = jvmNameToJavaName(assistClass);
        if (objectName.length() == 0) {
            throw new IllegalArgumentException("invalid signature. objectName not found :" + signature);
        }
        return objectName;
    }

    private static void getDescriptor(final StringBuilder buf, final Class<?> c) {
        Class<?> d = c;
        while (true) {
            if (d.isPrimitive()) {
                char car;
                if (d == Integer.TYPE) {
                    car = 'I';
                } else if (d == Void.TYPE) {
                    car = 'V';
                } else if (d == Boolean.TYPE) {
                    car = 'Z';
                } else if (d == Byte.TYPE) {
                    car = 'B';
                } else if (d == Character.TYPE) {
                    car = 'C';
                } else if (d == Short.TYPE) {
                    car = 'S';
                } else if (d == Double.TYPE) {
                    car = 'D';
                } else if (d == Float.TYPE) {
                    car = 'F';
                } else /* if (d == Long.TYPE) */{
                    car = 'J';
                }
                buf.append(car);
                return;
            } else if (d.isArray()) {
                buf.append('[');
                d = d.getComponentType();
            } else {
                buf.append('L');
                String name = d.getName();
                int len = name.length();
                for (int i = 0; i < len; ++i) {
                    char car = name.charAt(i);
                    buf.append(car == '.' ? '/' : car);
                }
                buf.append(';');
                return;
            }
        }
    }
}
