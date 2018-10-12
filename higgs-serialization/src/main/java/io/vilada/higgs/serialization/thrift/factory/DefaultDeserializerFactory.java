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

package io.vilada.higgs.serialization.thrift.factory;

import org.apache.thrift.TDeserializer;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;

/**
 * @author mjolnir
 */
public final class DefaultDeserializerFactory implements DeserializerFactory<TDeserializer> {

    public static final DefaultDeserializerFactory INSTANCE = new DefaultDeserializerFactory();

    private static TProtocolFactory protocolFactory = new TCompactProtocol.Factory();

    public TDeserializer createDeserializer() {
        return new TDeserializer(protocolFactory);
    }

}
