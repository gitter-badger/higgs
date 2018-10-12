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

package io.vilada.higgs.common.trace;

public final class SpanConstant {

    /**
     * cross process call for client
     */
    public static final String SPAN_COMPONENT_TARGET = "component.target";

    /**
     * cross process call for server
     */
    public static final String SPAN_COMPONENT_DESTINATION = "component.destination";

    /**
     * db sql bind params
     */
    public static final String SPAN_TAG_DB_PARAM = "db.param";

    /**
     * http bind params
     */
    public static final String SPAN_TAG_HTTP_PARAM = "http.param";

    /**
     * http client ip
     */
    public static final String SPAN_TAG_HTTP_CLIENTIP = "http.client_ip";

    /**
     * http user agent header
     */
    public static final String SPAN_TAG_HTTP_USER_AGENT = "http.user_agent";

    /**
     * http host header
     */
    public static final String SPAN_TAG_HTTP_HOST = "http.host";

    /**
     * http port header
     */
    public static final String SPAN_TAG_HTTP_PORT = "http.port";

    /**
     * http response uri
     */
    public static final String SPAN_TAG_HTTP_RESPONSE_URI = "http.response.uri";

    /**
     * http proxy host header
     */
    public static final String SPAN_TAG_HTTP_PROXY_HOST = "http.proxy.host";

    /**
     * http proxy port header
     */
    public static final String SPAN_TAG_HTTP_PROXY_PORT = "http.proxy.port";

    /**
     * http referer header
     */
    public static final String SPAN_TAG_HTTP_REFERER = "http.referer";

    /**
     * peer address
     */
    public static final String SPAN_TAG_PEER_ADDRESS = "peer.address";

    /**
     * span referer
     */
    public static final String SPAN_CONTEXT_REFERER_DELIMITER = "<:>";

    /**
     * http x-forwarded-for header
     */
    public static final String SPAN_TAG_HTTP_XFORWARDED = "http.x_forwarded_for";

    /**
     * The type or "kind" of an error (only for event="error" logs). E.g., "Exception", "OSError"
     */
    public static final String SPAN_LOG_ERROR_KIND = "error.kind";

    /**
     *
     * For languages that support such a thing (e.g., Java, Python),
     * the actual Throwable/Exception/Error object instance itself. E.g.,
     * A java.lang.UnsupportedOperationException instance,
     * a python exceptions.NameError instance
     *
     */
    public static final String SPAN_LOG_ERROR_OBJECT = "error.object";

    /**
     *
     * A stable identifier for some notable moment in the lifetime of a Span.
     * For instance, a mutex lock acquisition or release
     * or the sorts of lifetime events in a browser page load described in the Performance.
     *
     * timing specification. E.g.,
     * from Zipkin, "cs", "sr", "ss", or "cr". Or,
     *
     * more generally, "initialized" or "timed out".
     *
     * For errors, "error"
     *
     */
    public static final String SPAN_LOG_EVENT = "event";

    /**
     *
     * A concise, human-readable,
     * one-line message explaining the event.
     * E.g., "Could not connect to backend", "Cache invalidation succeeded"
     *
     */
    public static final String SPAN_LOG_MESSAGE = "message";

    /**
     *
     * A stack trace in platform-conventional format;
     * may or may not pertain to an error. E.g.,
     *
     * "File \"example.py\",
     * line 7, in \<module\>\ncaller()\nFile \"example.py\",
     * line 5, in caller\ncallee()\nFile \"example.py\",
     * line 2, in callee\nraise Exception(\"Yikes\")\n"
     *
     */
    public static final String SPAN_LOG_STACK = "stack";

}
