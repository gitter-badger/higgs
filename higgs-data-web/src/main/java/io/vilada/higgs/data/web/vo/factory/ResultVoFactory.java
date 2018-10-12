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

import io.vilada.higgs.data.web.vo.ResultVO;
import io.vilada.higgs.data.web.vo.enums.VoMessageEnum;
import io.vilada.higgs.data.web.vo.enums.VoStatusEnum;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-08-23 下午4:05
 **/

public class ResultVoFactory {

    public static <T> ResultVO<T> getSuccessResultVo() {
        ResultVO<T> resultVO = new ResultVO<T>();
        resultVO.setCode(VoMessageEnum.SUCCESS.getCode());
        resultVO.setStatus(VoStatusEnum.SUCCESS.getStatus());
        resultVO.setMessage(VoMessageEnum.SUCCESS.getMessage());

        return resultVO;
    }

    public static <T> ResultVO<T> getSuccessResultVo(T data) {
        ResultVO<T> resultVO = new ResultVO<T>();
        resultVO.setCode(VoMessageEnum.SUCCESS.getCode());
        resultVO.setStatus(VoStatusEnum.SUCCESS.getStatus());
        resultVO.setMessage(VoMessageEnum.SUCCESS.getMessage());
        resultVO.setData(data);

        return resultVO;
    }

    public static <T> ResultVO<T> getFailedResultVo() {
        ResultVO<T> resultVO = new ResultVO<T>();
        resultVO.setCode(VoMessageEnum.FAILED.getCode());
        resultVO.setStatus(VoStatusEnum.FAILED.getStatus());
        resultVO.setMessage(VoMessageEnum.FAILED.getMessage());

        return resultVO;
    }

    public static <T> ResultVO<T> getFailedResultVo(VoMessageEnum voMessageEnum) {
        ResultVO<T> resultVO = new ResultVO<T>();
        resultVO.setStatus(VoStatusEnum.FAILED.getStatus());
        resultVO.setMessage(voMessageEnum.getMessage());
        resultVO.setCode(voMessageEnum.getCode());

        return resultVO;
    }

    public static <T> ResultVO<T> getFailedResultVo(VoMessageEnum voMessageEnum, String message) {
        ResultVO<T> resultVO = new ResultVO<T>();
        resultVO.setStatus(VoStatusEnum.FAILED.getStatus());
        resultVO.setMessage(message);
        resultVO.setCode(voMessageEnum.getCode());

        return resultVO;
    }

}
