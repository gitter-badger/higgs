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

import io.vilada.higgs.data.service.bo.in.TransChainSnapShotInBO;
import io.vilada.higgs.data.service.bo.out.transactionchain.overview.TransactionChainOverviewBO;
import io.vilada.higgs.data.service.elasticsearch.service.DataServiceTestContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolationException;

/**
 * @author Junjie Peng
 * @date 2018-3-7
 */

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest()
@ContextConfiguration(classes = DataServiceTestContext.class)
public class TransChainSnapOverServiceTest {
    @Autowired
    private TransChainSnapOverService transChainSnapOverService;


    @Test
    public void testNormalRequest() {
        TransChainSnapShotInBO inBO = new TransChainSnapShotInBO();
        inBO.setAppId("722905091556839424");
        inBO.setInstanceId("726617650769596416");
        inBO.setRequestId("kDhrG9Mr-43-660d9443b-17fd");
        inBO.setTierId("722905376010342400");
        inBO.setTraceId("kDhrG9Mr-43-660d9443b-17fc");

        TransactionChainOverviewBO outBO = transChainSnapOverService.getTransChainSnapShot(inBO);

        /*
        Assert.assertNotNull(outBO);
        Assert.assertEquals("/goods.do", outBO.getBasicInfo().getTransaction());
        List<SlowestModule> slowestModuleList = outBO.getPotentialIssue().getSlowestModule();
        Assert.assertEquals(1, slowestModuleList.size());
        Assert.assertEquals("kDhrG9Mr-43-660d9443b-17ff",
                slowestModuleList.get(0).getRequestId());
        Assert.assertEquals(0, outBO.getPotentialIssue().getSlowestSqlRequest().size());
        Assert.assertEquals(0, outBO.getPotentialIssue().getSlowestRemoteInvocation().size());*/
    }

    @Test
    public void testNonExistedRequestId() {
        TransChainSnapShotInBO inBO = new TransChainSnapShotInBO();
        inBO.setAppId("722905091556839424");
        inBO.setInstanceId("726617650769596416");
        inBO.setRequestId("not-existed-request-id");
        inBO.setTierId("722905376010342400");
        inBO.setTraceId("kDhrG9Mr-43-660d9443b-17fc");

        TransactionChainOverviewBO outBO = transChainSnapOverService.getTransChainSnapShot(inBO);
        /*
        Assert.assertNotNull(outBO);
        Assert.assertNull(outBO.getBasicInfo().getTransaction());
        Assert.assertNull(outBO.getRequestParam().getParams());
        Assert.assertEquals(0, outBO.getPotentialIssue().getSlowestModule().size());
        Assert.assertEquals(0, outBO.getPotentialIssue().getSlowestSqlRequest().size());
        Assert.assertEquals(0, outBO.getPotentialIssue().getSlowestRemoteInvocation().size());*/
    }

    @Test(expected = ConstraintViolationException.class)
    public void testNullAppId() {
        TransChainSnapShotInBO inBO = new TransChainSnapShotInBO();
        inBO.setAppId(null);
        inBO.setInstanceId("726617650769596416");
        inBO.setRequestId("kDhrG9Mr-43-660d9443b-17fd");
        inBO.setTierId("722905376010342400");
        inBO.setTraceId("kDhrG9Mr-43-660d9443b-17fc");

        transChainSnapOverService.getTransChainSnapShot(inBO);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testNullRequestId() {
        TransChainSnapShotInBO inBO = new TransChainSnapShotInBO();
        inBO.setAppId("722905091556839424");
        inBO.setInstanceId("726617650769596416");
        inBO.setRequestId(null);
        inBO.setTierId("722905376010342400");
        inBO.setTraceId("kDhrG9Mr-43-660d9443b-17fc");

        transChainSnapOverService.getTransChainSnapShot(inBO);
    }

}
