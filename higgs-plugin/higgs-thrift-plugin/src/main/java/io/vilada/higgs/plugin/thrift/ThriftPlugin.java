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

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.instrument.transformer.DefaultTransformCallback;
import io.vilada.higgs.agent.common.instrument.transformer.TransformCallback;
import io.vilada.higgs.agent.common.instrument.transformer.TransformTemplate;
import io.vilada.higgs.agent.common.plugin.ProfilerPlugin;
import io.vilada.higgs.agent.common.trace.HiggsContinuationAccessor;
import io.vilada.higgs.plugin.thrift.interceptor.client.*;
import io.vilada.higgs.plugin.thrift.interceptor.protocol.*;
import io.vilada.higgs.plugin.thrift.interceptor.server.*;

/**
 * @author cyp
 */
public class ThriftPlugin implements ProfilerPlugin {
	private TransformTemplate transformTemplate;

    public void setup(ProfilerConfig profilerConfig, TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
        addInterceptorsForServerServe();
        addInterceptorsForSynchronousClients();
        addInterceptorsForAsynchronousClients();

		addInterceptorsForSynchronousProcessors();
		addInterceptorsForAsynchronousProcessors();
		//addInterceptorsForRetrievingSocketAddresses();
		addTProtocolEditors();
	}

	private void addInterceptorsForServerServe() {
		String[] targetArr = new String[]{"org.apache.thrift.server.AbstractNonblockingServer",
				"org.apache.thrift.server.TSimpleServer",
				"org.apache.thrift.server.TThreadPoolServer"};
        String servInterceptor = ServerServeInterceptor.class.getName();
        for (String target : targetArr) {
			TransformCallback callback = new DefaultTransformCallback(target);
			callback.addInterceptor("serve", "()V", servInterceptor);
			transformTemplate.transform(callback);
		}
    }


    private void addInterceptorsForSynchronousClients() {
        TransformCallback callback = new DefaultTransformCallback("org.apache.thrift.TServiceClient");
        callback.addInterceptor("sendBase", "(Ljava/lang/String;Lorg/apache/thrift/TBase;)V", SyncSendBaseInterceptor.class.getName());
        callback.addInterceptor("receiveBase", "(Lorg/apache/thrift/TBase;Ljava/lang/String;)V", SyncReceiveBaseInterceptor.class.getName());
        transformTemplate.transform(callback);
    }

	private void addInterceptorsForAsynchronousClients() {
		TransformCallback callback = new DefaultTransformCallback("org.apache.thrift.async.TAsyncClientManager");
		callback.addInterceptor("call", "(Lorg/apache/thrift/async/TAsyncMethodCall;)V", AsyncManagerCallInterceptor.class.getName());
		transformTemplate.transform(callback);

        callback = new DefaultTransformCallback("org.apache.thrift.async.TAsyncMethodCall");
        callback.addField(HiggsContinuationAccessor.class.getName());
        callback.addInterceptor("cleanUpAndFireCallback", "(Ljava/nio/channels/SelectionKey;)V", AsyncCleanUpAndFireCallbackInterceptor.class.getName());
        callback.addInterceptor("onError", "(Ljava/lang/Exception;)V", AsyncCallOnErrorInterceptor.class.getName());
        callback.addInterceptor("start", "(Ljava/nio/channels/Selector;)V", AsyncCallStartInterceptor.class.getName());
        transformTemplate.transform(callback);
	}

	// Processor - synchronous
	private void addInterceptorsForSynchronousProcessors() {
		TransformCallback callback = new DefaultTransformCallback("org.apache.thrift.TBaseProcessor");
		callback.addInterceptor("process",
				"(Lorg/apache/thrift/protocol/TProtocol;Lorg/apache/thrift/protocol/TProtocol;)Z",
				SyncProcessorInterceptor.class.getName());
		transformTemplate.transform(callback);

        callback = new DefaultTransformCallback("org.apache.thrift.ProcessFunction");
        callback.addInterceptor("process",
				"(ILorg/apache/thrift/protocol/TProtocol;Lorg/apache/thrift/protocol/TProtocol;Ljava/lang/Object;)V",
				SyncFunctionInterceptor.class.getName());
        transformTemplate.transform(callback);
	}

	// Processor - asynchronous
	private void addInterceptorsForAsynchronousProcessors() {
		TransformCallback callback = new DefaultTransformCallback("org.apache.thrift.TBaseAsyncProcessor");
		callback.addInterceptor("process",
				"(Lorg/apache/thrift/protocol/TProtocol;Lorg/apache/thrift/protocol/TProtocol;)Z",
				AsyncProcessorInterceptor.class.getName());
		transformTemplate.transform(callback);

//		transformTemplate.transform("org.apache.thrift.AsyncProcessFunction", new DefaultTransformCallback() {
//			public void doInTransform(Instrumenter instrumenter, byte[] classfileBuffer) {
//				instrumenter.instrumentMethod(AsyncFunctionInterceptor.class.getName(), "start", "int",
//						"org.apache.thrift.protocol.TProtocol", "org.apache.thrift.protocol.TProtocol",
//						"java.lang.Object");
//			}
//		});
	}

//	// Common
//	private void addInterceptorsForRetrievingSocketAddresses() {
//		TransformCallback callback = new DefaultTransformCallback("org.apache.thrift.transport.TSocket");
//		callback.addInterceptor("<init>", "(Ljava/net/TSocket;)V", SocketConstructInterceptor.class.getName());
//		callback.addInterceptor("<init>", "(Ljava/lang/String;II)V", SocketConstructInterceptor.class.getName());
//		transformTemplate.transform(callback);
//
//		callback = new DefaultTransformCallback("org.apache.thrift.transport.TFramedTransport");
//		callback.addInterceptor("<init>", "(Lorg/apache/thrift/transport/TTransport;)V", SocketConstructInterceptor.class.getName());
//		callback.addInterceptor("<init>", "(Lorg/apache/thrift/transport/TTransport;I)V", SocketFramedTransportConstructInterceptor.class.getName());
//		transformTemplate.transform(callback);
//
//		callback = new DefaultTransformCallback("org.apache.thrift.transport.TFastFramedTransport");
//		callback.addInterceptor("<init>", "(Lorg/apache/thrift/transport/TTransport;II)V", SocketFastFramedTransportConstructInterceptor.class.getName());
//		transformTemplate.transform(callback);
//
//		callback = new DefaultTransformCallback("org.apache.thrift.transport.TSaslClientTransport");
//		callback.addInterceptor("<init>", "(Lorg/apache/thrift/transport/TTransport;)V", SocketSaslTransportConstructInterceptor.class.getName());
//		callback.addInterceptor("<init>", "(Ljavax/security/sasl/SaslClient;Lorg/apache/thrift/transport/TTransport;)V", SocketSaslTransportConstructInterceptor.class.getName());
//		transformTemplate.transform(callback);
//
////		transformTemplate.transform("org.apache.thrift.transport.TMemoryInputTransport", new DefaultTransformCallback() {
////			public void doInTransform(Instrumenter instrumenter, byte[] classfileBuffer) {
////				//instrumenter.addField("FIELD_ACCESSOR_SOCKET");
////			}
////		});
////
////		transformTemplate.transform("org.apache.thrift.transport.TIOStreamTransport", new DefaultTransformCallback() {
////			public void doInTransform(Instrumenter instrumenter, byte[] classfileBuffer) {
////				//instrumenter.addField("FIELD_ACCESSOR_SOCKET");
////			}
////		});
//		// nonblocking
//        callback = new DefaultTransformCallback("org.apache.thrift.transport.TNonblockingSocket");
//        callback.addInterceptor("<init>", "(Ljava/nio/channels/SocketChannel;ILjava/net/SocketAddress;)V", SocketSaslTransportConstructInterceptor.class.getName());
//        transformTemplate.transform(callback);
//
//		addFrameBufferEditor();
//	}

//	private void addFrameBufferEditor() {
//		transformTemplate.transform(new DefaultTransformCallback("org.apache.thrift.server.AbstractNonblockingServer$FrameBuffer") {
//			public void doInTransform(Instrumenter instrumenter, byte[] classfileBuffer) {
//				//instrumenter.addField(ThriftConstants.FIELD_ACCESSOR_SOCKET);
//				//instrumenter.addGetter(ThriftConstants.FIELD_GETTER_T_NON_BLOCKING_TRANSPORT,ThriftConstants.FRAME_BUFFER_FIELD_TRANS_);
//				if(instrumenter.getInstrumentClass().hasField(ThriftConstants.FRAME_BUFFER_FIELD_IN_TRANS_)){
//					//instrumenter.addGetter(ThriftConstants.FIELD_GETTER_T_TRANSPORT,ThriftConstants.FRAME_BUFFER_FIELD_IN_TRANS_);
//					instrumenter.instrumentConstructor(ServerSpanAroundInterceptor.class.getName(),
//							"org.apache.thrift.server.AbstractNonblockingServer",
//							"org.apache.thrift.transport.TNonblockingTransport",
//							"org.apache.thrift.server.AbstractNonblockingServer$AbstractSelectThread"
//							);
//				}
//				if(instrumenter.getInstrumentClass().hasMethod("getInputTransport", "org.apache.thrift.transport.TTransport")){
//					//instrumenter.addGetter(ThriftConstants.FIELD_GETTER_T_TRANSPORT,ThriftConstants.FRAME_BUFFER_FIELD_IN_TRANS_);
//					instrumenter.instrumentMethod(ServerSpanAroundInterceptor.class.getName(), "getInputTransport","org.apache.thrift.transport.TTransport");
//				}
//			}
//		});
//	}

	// Common - protocols
	private void addTProtocolEditors() {
		String[] targetArr = new String[]{"org.apache.thrift.protocol.TBinaryProtocol",
				"org.apache.thrift.protocol.TCompactProtocol","org.apache.thrift.protocol.TJSONProtocol"};
		for(String target : targetArr) {
            TransformCallback callback = new DefaultTransformCallback(target);
            callback.addInterceptor("writeFieldStop", "()V", ProtocolWriteFieldStopInterceptor.class.getName());
            callback.addInterceptor("writeMessageBegin", "(Lorg/apache/thrift/protocol/TMessage;)V", ProtocolWriteMessageBeginInterceptor.class.getName());
            callback.addInterceptor("readFieldBegin", "()Lorg/apache/thrift/protocol/TField;", ProtocolReadFieldBeginInterceptor.class.getName());
            callback.addInterceptor("readBinary", "()Ljava/nio/ByteBuffer;", ProtocolReadTTypeInterceptor.class.getName());
            callback.addInterceptor("readMessageEnd", "()V", ProtocolReadMessageEndInterceptor.class.getName());
            callback.addInterceptor("readMessageBegin", "()Lorg/apache/thrift/protocol/TMessage;", ProtocolReadMessageBeginInterceptor.class.getName());
            transformTemplate.transform(callback);
		}
	}
}
