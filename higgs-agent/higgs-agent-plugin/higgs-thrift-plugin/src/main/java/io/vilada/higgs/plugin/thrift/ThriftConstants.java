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

package io.vilada.higgs.plugin.thrift;

import java.nio.charset.Charset;

/**
 * @author HyunGil Jeong
 */
public final class ThriftConstants {
    private ThriftConstants() {
    }
    public static final String UNKNOWN_METHOD_NAME = "unknown";
    public static final String UNKNOWN_METHOD_URI = "/" + UNKNOWN_METHOD_NAME;
    public static final String UNKNOWN_ADDRESS = "Unknown";

    // field names
    public static final String T_ASYNC_METHOD_CALL_FIELD_TRANSPORT = "transport";
    public static final String FRAME_BUFFER_FIELD_TRANS_ = "trans_";
    public static final String FRAME_BUFFER_FIELD_IN_TRANS_ = "inTrans_";

    // custom field injector (accessor) FQCN
    private static final String FIELD_ACCESSOR_BASE = "com.navercorp.pinpoint.plugin.thrift.field.accessor.";
    public static final String FIELD_ACCESSOR_ASYNC_MARKER_FLAG = FIELD_ACCESSOR_BASE + "AsyncMarkerFlagFieldAccessor";
    public static final String FIELD_ACCESSOR_SERVER_MARKER_FLAG = FIELD_ACCESSOR_BASE + "ServerMarkerFlagFieldAccessor";
    public static final String FIELD_ACCESSOR_SOCKET_ADDRESS = FIELD_ACCESSOR_BASE + "SocketAddressFieldAccessor";
    public static final String FIELD_ACCESSOR_SOCKET = FIELD_ACCESSOR_BASE + "SocketFieldAccessor";

    // field getter FQCN
    private static final String FIELD_GETTER_BASE = "com.navercorp.pinpoint.plugin.thrift.field.getter.";
    public static final String FIELD_GETTER_T_NON_BLOCKING_TRANSPORT = FIELD_GETTER_BASE + "TNonblockingTransportFieldGetter";
    public static final String FIELD_GETTER_T_TRANSPORT = FIELD_GETTER_BASE + "TTransportFieldGetter";
    public static final String FIELD_GETTER_T_PROTOCOL = FIELD_GETTER_BASE + "TProtocolFieldGetter";

    // added for tracedata
    public static final String THRIFT_RPC_METHOD_NAME="thrift_rpc_method_name";
    public static final String THRIFT_HEADER_TOBE_READ="thrift_header_tobe_read";
    public static final String THRIFT_MSG_BEGIN_TIME="thrift_msg_begin_time";
    public static final String THRIFT_SYNC_FUNC_CALLED = "thrift_sync_func_called";
    public static final String THRIFT_ASYNC_FUNC_CALLED = "thrift_async_func_called";

    public static final Charset UTF_8 = Charset.forName("UTF-8");
}