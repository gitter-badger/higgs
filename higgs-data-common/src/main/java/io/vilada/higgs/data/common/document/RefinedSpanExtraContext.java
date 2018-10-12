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

package io.vilada.higgs.data.common.document;

import io.vilada.higgs.common.trace.LayerEnum;
import io.vilada.higgs.data.common.constant.TypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author ethan
 */
@Setter
@Getter
public class RefinedSpanExtraContext implements Serializable {

    public String parentInstanceId;

    public String parentTierId;

    public String parentAppId;

    public String childInstanceId;

    public String childTierId;

    public String childAppId;

    public List<String> spanRefererList;

    public String spanTransactionName;

    public String agentTransactionName;

    public String component;

    public String operationType;

    public TypeEnum type;

    public LayerEnum layer;

    public String address;

    public Boolean transformed;

    public int elapsed;

    public int selfElapsed;

    // ignore children instance
    public int instanceInternalElapsed;

    // igonre children instance, rpc, db
    public int instanceInternalIgnoreRemoteCallElapsed;

    public boolean traceError;

    public boolean instanceRoot;

    public boolean tierRoot;

    public boolean appRoot;

}
