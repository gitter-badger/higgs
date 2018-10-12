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

package io.vilada.higgs.data.service.enums;

import io.vilada.higgs.common.trace.LayerEnum;
import lombok.Getter;

/**
 * @author yawei
 * @date 2017-12-4.
 */
@Getter
public enum ComponentCallersTypeEnum {
    DATABASE(2, LayerEnum.SQL, LayerEnum.NO_SQL),REMOTE(3, LayerEnum.HTTP, LayerEnum.RPC);

    private int type;

    private LayerEnum[] refinedSpanTypeEnum;

    ComponentCallersTypeEnum(int type, LayerEnum ... refinedSpanTypeEnum){
        this.type = type;
        this.refinedSpanTypeEnum = refinedSpanTypeEnum;
    }
    public static ComponentCallersTypeEnum getComponentCallersTypeEnum(int type) {
        ComponentCallersTypeEnum[] componentCallersTypeEnums = ComponentCallersTypeEnum.values();
        for (ComponentCallersTypeEnum componentCallersTypeEnum : componentCallersTypeEnums) {
            if (componentCallersTypeEnum.getType() == type) {
                return componentCallersTypeEnum;
            }
        }
        return null;
    }
}
