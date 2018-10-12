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

package io.vilada.higgs.data.service.elasticsearch.service.v2.transaction;

import io.vilada.higgs.data.service.bo.in.v2.Sort;
import io.vilada.higgs.data.service.bo.in.v2.remote.RemoteCallListInBO;
import io.vilada.higgs.data.service.bo.out.v2.remote.RemoteCallListOutBO;
import io.vilada.higgs.data.service.elasticsearch.service.DataServiceTestContext;
import io.vilada.higgs.data.service.elasticsearch.service.v2.remote.RemoteCallService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolationException;
import java.util.List;

/**
 * @author Junjie Peng
 * @date 2018-3-7
 */

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest()
@ContextConfiguration(classes = DataServiceTestContext.class)
public class RemoteCallServiceTest {
    @Autowired
    private RemoteCallService remoteCallService;

    @Test
    public void testNormalRequest() {
        RemoteCallListInBO remoteCallListInBO = new RemoteCallListInBO();
        remoteCallListInBO.setAppId("722905091556839424");
        remoteCallListInBO.setStartTime(1518313520783L);
        remoteCallListInBO.setEndTime(1520905520783L);

        Sort sort = new Sort();
        sort.setField("avgResponseTime");
        sort.setOrder("desc");

        List<RemoteCallListOutBO> outBO = remoteCallService.list(remoteCallListInBO, sort);
//        Assert.assertNotNull(outBO);
//        Assert.assertEquals(2, outBO.size());
//        RemoteCallListOutBO callListOutBO = outBO.get(0);
//        Assert.assertEquals("bj-rc-msa-test-demo-v-test-1.host.dataengine.com:9001", callListOutBO.getAddress());
//        Assert.assertEquals(8871L, callListOutBO.getRequestCount().longValue());
//        Assert.assertEquals(68, callListOutBO.getAvgResponseTime(), 1);
    }

    @Test
    public void testNonData() {
        RemoteCallListInBO remoteCallListInBO = new RemoteCallListInBO();
        remoteCallListInBO.setAppId("722905091556839424");
        remoteCallListInBO.setStartTime(1518313520783L);
        remoteCallListInBO.setEndTime(remoteCallListInBO.getStartTime() + 1);

        Sort sort = new Sort();
        sort.setField("avgResponseTime");
        sort.setOrder("desc");

        List<RemoteCallListOutBO> outBO = remoteCallService.list(remoteCallListInBO, sort);
        Assert.assertNull(outBO);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testNullAppId() {
        RemoteCallListInBO remoteCallListInBO = new RemoteCallListInBO();
        remoteCallListInBO.setAppId(null);
        remoteCallListInBO.setStartTime(1518313520783L);
        remoteCallListInBO.setEndTime(1520905520783L);

        Sort sort = new Sort();
        sort.setField("avgResponseTime");
        sort.setOrder("desc");

        remoteCallService.list(remoteCallListInBO, sort);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testRequestWithInvalidTime() {
        RemoteCallListInBO remoteCallListInBO = new RemoteCallListInBO();
        remoteCallListInBO.setAppId("722905091556839424");
        remoteCallListInBO.setStartTime(0L);
        remoteCallListInBO.setEndTime(-1L);

        Sort sort = new Sort();
        sort.setField("avgResponseTime");
        sort.setOrder("desc");

        remoteCallService.list(remoteCallListInBO, sort);
    }
}