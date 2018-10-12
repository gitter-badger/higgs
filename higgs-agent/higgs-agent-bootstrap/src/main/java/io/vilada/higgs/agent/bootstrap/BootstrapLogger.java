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

package io.vilada.higgs.agent.bootstrap;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;

/**
 * only for bootstrap, base system.in and system.our implelement
 * @author mjolnir
 */
public final class BootstrapLogger {

    private final String messagePattern;
    private final PrintStream out;
    private final PrintStream err;


    private BootstrapLogger(String loggerName) {
        this(loggerName, System.out, System.err);
    }

    private BootstrapLogger(String loggerName, PrintStream out, PrintStream err) {
        if (loggerName == null) {
            throw new NullPointerException("loggerName must not be null");
        }
        this.messagePattern = "{0,date,yyyy-MM-dd HH:mm:ss} [{1}](" + loggerName + ") {2}{3}";
        this.out = out;
        this.err = err;
    }

    public static BootstrapLogger getLogger(String loggerName) {
        return new BootstrapLogger(loggerName);
    }

    private String format(String logLevel, String msg, String exceptionMessage) {
        exceptionMessage = defaultString(exceptionMessage);

        MessageFormat messageFormat = new MessageFormat(messagePattern);
        final long date = System.currentTimeMillis();
        Object[] parameter = {date, logLevel, msg, exceptionMessage};
        return messageFormat.format(parameter);
    }

    public void info(String msg) {
        String formatMessage = format("INFO ", msg, "");
        this.out.println(formatMessage);
    }

    public void warn(String msg) {
        warn(msg, null);
    }

    public void warn(String msg, Throwable throwable) {
        String exceptionMessage = toString(throwable);
        String formatMessage = format("WARN ", msg, exceptionMessage);
        this.err.println(formatMessage);
    }

    private String toString(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println();
        throwable.printStackTrace(pw);
        pw.close();
        return sw.toString();
    }

    private String defaultString(String exceptionMessage) {
        if (exceptionMessage == null) {
            return "";
        }
        return exceptionMessage;
    }
}
