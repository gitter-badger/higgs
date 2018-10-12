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

package io.vilada.higgs.data.web.vo.factory;

import io.vilada.higgs.data.web.vo.BaseOutVO;
import io.vilada.higgs.data.web.vo.TimeSeriesVO;
import io.vilada.higgs.data.web.vo.in.TimeInVO;
import io.vilada.higgs.data.web.service.enums.DataCommonVOMessageEnum;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-07-04 下午5:42
 **/
public class VOFactory {

    public static BaseOutVO getBaseOutVO(Integer code, String errorMessage) {
        BaseOutVO baseOutVo = new BaseOutVO();
        baseOutVo.setCode(code);
        baseOutVo.setMessage(errorMessage);

        return baseOutVo;
    }

    public static BaseOutVO getSuccessBaseOutVO() {
        BaseOutVO baseOutVo = new BaseOutVO();
        baseOutVo.setCode(DataCommonVOMessageEnum.COMMON_SUCCESS.getCode());
        baseOutVo.setMessage(DataCommonVOMessageEnum.COMMON_SUCCESS.getMessage());

        return baseOutVo;
    }

    public static BaseOutVO getSuccessBaseOutVO(Object data) {
        BaseOutVO baseOutVo = new BaseOutVO();
        baseOutVo.setCode(DataCommonVOMessageEnum.COMMON_SUCCESS.getCode());
        baseOutVo.setData(data);
        return baseOutVo;
    }

    public static TimeSeriesVO getTimeSeriesVo(Object data, TimeInVO timeInVO, String unit) {

        TimeSeriesVO timeSeriesVO = new TimeSeriesVO();
        timeSeriesVO.setEndTime(timeInVO.getEndTime());
        timeSeriesVO.setStartTime(timeInVO.getStartTime());
        timeSeriesVO.setUnit(unit);
        timeSeriesVO.setData(data);
        return timeSeriesVO;
    }
}
