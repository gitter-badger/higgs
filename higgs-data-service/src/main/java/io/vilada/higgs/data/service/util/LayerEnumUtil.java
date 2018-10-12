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

package io.vilada.higgs.data.service.util;

import io.vilada.higgs.common.trace.LayerEnum;

/**
 * @author yawei
 * @date 2017-12-12.
 */
public class LayerEnumUtil {

    public static boolean isDataBase(LayerEnum layer){
        if(layer != null && (layer == LayerEnum.SQL || layer == LayerEnum.NO_SQL)){
            return true;
        }
        return false;
    }

    public static boolean isRemote(LayerEnum layer){
        if(layer != null && (layer == LayerEnum.HTTP || layer == LayerEnum.RPC)){
            return true;
        }
        return false;
    }
}
