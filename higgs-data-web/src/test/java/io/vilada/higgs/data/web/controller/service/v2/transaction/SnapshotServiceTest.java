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

package io.vilada.higgs.data.web.controller.service.v2.transaction;

import io.vilada.higgs.data.web.controller.service.DataServiceTestContext;
import io.vilada.higgs.data.web.service.bo.out.v2.snapshot.SlowComponentBO;
import io.vilada.higgs.data.web.service.elasticsearch.service.v2.transaction.snapshot.SnapshotService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * 功能及特点的描述简述: APM快照ServiceTest
 * <p>
 * 项目名称: APM
 *
 * @author 丛树林
 * @date 2018/03/14
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest()
@ContextConfiguration(classes = DataServiceTestContext.class)
public class SnapshotServiceTest {
    @Autowired
    private SnapshotService snapshotService;

    @Test
    public void slowComponentByTracIdNormalRequestTest1() {
        String traceId ="kDhrG9Mr-24-6da501830-2f";
        Integer rishRatio = 20;

        List<SlowComponentBO> outBO = snapshotService.getSlowComponentByTracId(traceId, rishRatio);
//        Assert.assertNotNull(outBO);
//        Assert.assertEquals(3, outBO.size());
//        SlowComponentBO slowComponentBO = outBO.get(0);
//        Assert.assertEquals("kDhrG9Mr-24-6da501830-30", slowComponentBO.getRequestId());
//        Assert.assertEquals("SERVER", slowComponentBO.getType());
    }

    @Test
    public void slowComponentByTracIdNormalRequestTest2() {
        String traceId ="kDhrG9Mr-20-6d428415c-1cc8";
        Integer rishRatio = 10;

        List<SlowComponentBO> outBO = snapshotService.getSlowComponentByTracId(traceId, rishRatio);
//        Assert.assertNotNull(outBO);
//        Assert.assertEquals(1, outBO.size());
//        SlowComponentBO slowComponentBO = outBO.get(0);
//        Assert.assertEquals("SERVER", slowComponentBO.getType());
    }

    @Test
    public void slowComponentByTracIdNullTest() {
        String traceId ="";
        Integer rishRatio = 20;

        List<SlowComponentBO> outBO = snapshotService.getSlowComponentByTracId(traceId, rishRatio);
        //Assert.assertNull(outBO);

    }

}