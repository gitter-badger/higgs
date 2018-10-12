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

package io.vilada.higgs.data.web.service.bo.out.v2.snapshot;

import lombok.Data;

@Data
public class SlowComponentBO {
	
	private String requestId;
	private String appId;
	private String tierId;
	private String instanceId;
	private String traceId;
	private long totalTime;
	private String componentName;
	private String type;
	private double componentTime;
	private String tierName;
	private String apdex;
    /**
     * 入口span时间
     */
	private long transTime;

	private String errorName;
	private String errorMessage;

}
