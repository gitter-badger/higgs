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

package io.vilada.higgs.agent.common.logging;

/**
 * @author ethan
 */
public class DummyHiggsAgentLogger implements HiggsAgentLogger {

    public static final HiggsAgentLogger INSTANCE = new DummyHiggsAgentLogger();


    public void beforeInterceptor(Object target, String className, String methodName, String parameterDescription, Object[] args) {
    }


    public void beforeInterceptor(Object target, Object[] args) {
    }



    public void afterInterceptor(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result, Throwable throwable) {

    }


    public void afterInterceptor(Object target, Object[] args, Object result, Throwable throwable) {
    }



    public void afterInterceptor(Object target, String className, String methodName, String parameterDescription, Object[] args) {
    }


    public void afterInterceptor(Object target, Object[] args) {
    }


    public boolean isTraceEnabled() {
        return false;
    }


    public void trace(String msg) {

    }


    public void trace(String format, Object arg) {

    }


    public void trace(String format, Object arg1, Object arg2) {

    }


    public void trace(String format, Object[] argArray) {

    }


    public void trace(String msg, Throwable t) {

    }


    public boolean isDebugEnabled() {
        return false;
    }


    public void debug(String msg) {

    }


    public void debug(String format, Object arg) {

    }


    public void debug(String format, Object arg1, Object arg2) {

    }


    public void debug(String format, Object[] argArray) {

    }


    public void debug(String msg, Throwable t) {

    }


    public boolean isInfoEnabled() {
        return false;
    }


    public void info(String msg) {

    }


    public void info(String format, Object arg) {

    }


    public void info(String format, Object arg1, Object arg2) {

    }


    public void info(String format, Object[] argArray) {

    }


    public void info(String msg, Throwable t) {

    }


    public boolean isWarnEnabled() {
        return false;
    }


    public void warn(String msg) {

    }


    public void warn(String format, Object arg) {

    }


    public void warn(String format, Object[] argArray) {

    }


    public void warn(String format, Object arg1, Object arg2) {

    }


    public void warn(String msg, Throwable t) {

    }


    public boolean isErrorEnabled() {
        return false;
    }


    public void error(String msg) {

    }


    public void error(String format, Object arg) {

    }


    public void error(String format, Object arg1, Object arg2) {

    }


    public void error(String format, Object[] argArray) {

    }


    public void error(String msg, Throwable t) {

    }
}
