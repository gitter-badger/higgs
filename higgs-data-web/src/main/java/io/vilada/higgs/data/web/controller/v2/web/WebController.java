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

package io.vilada.higgs.data.web.controller.v2.web;

import io.vilada.higgs.data.web.service.bo.in.SearchCondition;
import io.vilada.higgs.data.web.controller.v2.util.ParamValidator;
import io.vilada.higgs.data.web.util.UnitEnum;
import io.vilada.higgs.data.web.vo.BaseOutVO;
import io.vilada.higgs.data.web.vo.enums.BrowserEnum;
import io.vilada.higgs.data.web.vo.factory.VOFactory;
import io.vilada.higgs.data.web.vo.in.TimeInVO;
import io.vilada.higgs.data.web.vo.out.LineOut;
import io.vilada.higgs.data.web.service.elasticsearch.service.web.WebAgentAjaxService;
import io.vilada.higgs.data.web.service.elasticsearch.service.web.WebAgentLoadService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.vilada.higgs.data.web.service.enums.DataCommonVOMessageEnum.CONDITION_INVALID;

@RestController
@RequestMapping(value = "/server/v2/web", produces = { "application/json;charset=UTF-8" })
public class WebController {

	private static final String AGENT_ID = "agentId";

	private static final String APP_ID = "appId";

	private static final String LOADED_TIME = "loadedTime";

	@Autowired
    WebAgentLoadService loadService;

	@Autowired
	WebAgentAjaxService ajaxService;

	private JSONObject parseJson(String s) {
		try {
			return JSONObject.parseObject(s);
		} catch (Exception e) {
			return null;
		}
	}

	private SearchCondition parseCondition(String s) {
		SearchCondition sc = null;
		JSONObject j = parseJson(s);
		if (j != null && j.containsKey("condition")) {
			sc = new SearchCondition();
			JSONObject condition = j.getJSONObject("condition");
			Long interval = Long.valueOf(condition.getString("aggrInterval"));
			if (interval == null) {
				if (condition.containsKey(AGENT_ID)) {
					sc.getEqCondition().put(AGENT_ID, condition.getString(AGENT_ID));
				}
				sc.setPage(condition.getIntValue("page"));
				sc.setSize(condition.getIntValue("size"));
				return sc;
			}
			sc.setStartTime(condition.getLongValue("startTime"));
			sc.setEndTime(condition.getLongValue("endTime"));
			sc.setInterval(interval);
			if(!ParamValidator.isTimeIntervalValid(sc.getStartTime(),
					sc.getEndTime(), sc.getInterval())){
				return null;
			}
			if (condition.containsKey(APP_ID)) {
				sc.getEqCondition().put(APP_ID, condition.getString(APP_ID));
			}
			sc.setPage(condition.getIntValue("page"));
			sc.setSize(condition.getIntValue("size"));
		}
		return sc;
	}

	private TimeInVO getTimeInVO(SearchCondition sc) {
		TimeInVO vo = new TimeInVO();
		vo.setAggrInterval(sc.getInterval());
		vo.setEndTime(sc.getStartTime());
		vo.setStartTime(sc.getEndTime());
		return vo;
	}

	private LineOut lineOut(List<Map<String, Object>> line, String lineName) {
		LineOut ln = new LineOut();
		ln.setName(lineName);
		ln.setList(line);
		return ln;
	}

	@RequestMapping(value = "/base/top5/pv")
	public BaseOutVO baseTop5Pv(@RequestBody String json) {
		SearchCondition sc = parseCondition(json);
		if (sc != null) {
			List<Map<String, Object>> top5 = loadService.topCt(sc, 5);
			List<LineOut> data = new ArrayList<>();
			if (top5 != null) {
				for (Map<String, Object> be : top5) {
					sc.getEqCondition().put("urlQuery", be.get("name").toString());
					data.add(lineOut(loadService.timelineCt(sc), be.get("name").toString()));
				}
			}
			return VOFactory.getTimeSeriesVo(data, getTimeInVO(sc), UnitEnum.RPM.getValue());
		} else {
			return VOFactory.getBaseOutVO(CONDITION_INVALID.getCode(), CONDITION_INVALID.getMessage());
		}
	}

	@RequestMapping(value = "/base/top5/timeSpend")
	public BaseOutVO baseTop5TimeSpend(@RequestBody String json) {
		SearchCondition sc = parseCondition(json);
		if (sc != null) {
			List<Map<String, Object>> top5 = loadService.top(LOADED_TIME, sc, 5);
			List<LineOut> data = new ArrayList<>();
			if (top5 != null) {
				for (Map<String, Object> be : top5) {
					sc.getEqCondition().put("urlQuery", be.get("name").toString());
					data.add(lineOut(loadService.timeline(LOADED_TIME, sc), be.get("name").toString()));
				}
			}
			return VOFactory.getTimeSeriesVo(data, getTimeInVO(sc), UnitEnum.MS.getValue());
		} else {
            return VOFactory.getBaseOutVO(CONDITION_INVALID.getCode(), CONDITION_INVALID.getMessage());
		}
	}

	@RequestMapping(value = "/base/pageLoad")
	public BaseOutVO pageLoad(@RequestBody String json) {
		SearchCondition sc = parseCondition(json);
		if (sc != null) {
			List<LineOut> data = new ArrayList<>();
			data.add(lineOut(loadService.timeline("firstScreenTime", sc), "firstScreenTime"));
			data.add(lineOut(loadService.timeline("whiteScreenTime", sc), "whiteScreenTime"));
			data.add(lineOut(loadService.timeline("operableTime", sc), "operableTime"));
			data.add(lineOut(loadService.timeline("resourceLoadedTime", sc), "resourceLoadedTime"));
			return VOFactory.getTimeSeriesVo(data, getTimeInVO(sc), UnitEnum.MS.getValue());
		} else {
            return VOFactory.getBaseOutVO(CONDITION_INVALID.getCode(), CONDITION_INVALID.getMessage());
		}
	}

	@RequestMapping(value = "/base/browser")
	public BaseOutVO baseBrowser(@RequestBody String json) {
		SearchCondition sc = parseCondition(json);
		if (sc != null) {
			List<LineOut> data = new ArrayList<>();
			for (BrowserEnum be : BrowserEnum.values()) {
				sc.getEqCondition().put("browser", be.toString().toLowerCase());
				data.add(lineOut(loadService.timeline(LOADED_TIME, sc), be.toString()));
			}
			return VOFactory.getTimeSeriesVo(data, getTimeInVO(sc), UnitEnum.MS.getValue());
		} else {
            return VOFactory.getBaseOutVO(CONDITION_INVALID.getCode(), CONDITION_INVALID.getMessage());
		}
	}

	@RequestMapping(value = "/base/apdex")
	public BaseOutVO baseApdex(@RequestBody String json) {
		SearchCondition sc = parseCondition(json);
		if (sc != null) {
			List<LineOut> data = new ArrayList<>();
			data.add(lineOut(loadService.timeLineApdex(sc), "None"));
			return VOFactory.getTimeSeriesVo(data, getTimeInVO(sc), "");
		} else {
            return VOFactory.getBaseOutVO(CONDITION_INVALID.getCode(), CONDITION_INVALID.getMessage());
		}
	}

	@RequestMapping(value = "/base/pv")
	public BaseOutVO basePv(@RequestBody String json) {
		SearchCondition sc = parseCondition(json);
		if (sc != null) {
			List<LineOut> data = new ArrayList<>();
			data.add(lineOut(loadService.timelineCt(sc), "None"));
			return VOFactory.getTimeSeriesVo(data, getTimeInVO(sc), UnitEnum.RPM.getValue());
		} else {
            return VOFactory.getBaseOutVO(CONDITION_INVALID.getCode(), CONDITION_INVALID.getMessage());
		}
	}

	@RequestMapping(value = "/ajax/pv")
	public BaseOutVO ajaxPv(@RequestBody String json) {
		SearchCondition sc = parseCondition(json);
		if (sc != null) {
			List<LineOut> data = new ArrayList<>();
			data.add(lineOut(ajaxService.timelineCt(sc), "None"));
			return VOFactory.getTimeSeriesVo(data, getTimeInVO(sc), UnitEnum.RPM.getValue());
		} else {
            return VOFactory.getBaseOutVO(CONDITION_INVALID.getCode(), CONDITION_INVALID.getMessage());
		}
	}

	@RequestMapping(value = "/ajax/time")
	public BaseOutVO ajaxTime(@RequestBody String json) {
		SearchCondition sc = parseCondition(json);
		if (sc != null) {
			List<LineOut> data = new ArrayList<>();
			data.add(lineOut(ajaxService.timeline(LOADED_TIME, sc), LOADED_TIME));
			return VOFactory.getTimeSeriesVo(data, getTimeInVO(sc), UnitEnum.MS.getValue());
		} else {
            return VOFactory.getBaseOutVO(CONDITION_INVALID.getCode(), CONDITION_INVALID.getMessage());
		}
	}
}
