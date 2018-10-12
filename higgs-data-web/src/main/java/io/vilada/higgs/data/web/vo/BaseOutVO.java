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

package io.vilada.higgs.data.web.vo;

import io.vilada.higgs.data.service.enums.DataCommonVOMessageEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-07-03 下午3:42
 **/

@Data
public class BaseOutVO implements Serializable {

    public static final BaseOutVO INSTANCE = new BaseOutVO();

    protected int code = 0;

    protected String message = "success";

    protected Object data;

    public static BaseOutVO withData(Object data) {
        BaseOutVO vo = new BaseOutVO();
        vo.setData(data);
        return vo;
    }

    public static BaseOutVO withMessageEnum(DataCommonVOMessageEnum messageEnum) {
        BaseOutVO vo = new BaseOutVO();
        vo.setCode(messageEnum.getCode());
        vo.setMessage(messageEnum.getMessage());
        return vo;
    }

}
