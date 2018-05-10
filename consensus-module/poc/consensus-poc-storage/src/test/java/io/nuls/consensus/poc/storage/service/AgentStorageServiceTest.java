/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.nuls.consensus.poc.storage.service;

import io.nuls.consensus.poc.storage.BaseTest;
import io.nuls.consensus.poc.storage.po.AgentPo;
import io.nuls.kernel.lite.core.SpringLiteContext;
import io.nuls.kernel.model.NulsDigestData;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by ln on 2018/5/10.
 */
public class AgentStorageServiceTest extends BaseTest {

    private AgentStorageService agentStorageService;

    @Before
    public void init() {
        agentStorageService = SpringLiteContext.getBean(AgentStorageService.class);
    }

    @Test
    public void testSave() {
        assertNotNull(agentStorageService);

        NulsDigestData hash = NulsDigestData.calcDigestData(new byte[20]);

        AgentPo agentPo = new AgentPo();
        agentPo.setAgentAddress(new byte[20]);
        agentPo.setHash(hash);

        boolean success = agentStorageService.save(agentPo);

        assert(success);
    }

    @Test
    public void testGet() {
        assertNotNull(agentStorageService);

        testSave();

        NulsDigestData hash = NulsDigestData.calcDigestData(new byte[20]);

        AgentPo agentPo = agentStorageService.get(hash);

        assertNotNull(agentPo);

        assert(Arrays.equals(agentPo.getAgentAddress(), new byte[20]));
    }

    @Test
    public void testDelete() {
        assertNotNull(agentStorageService);

        testSave();

        NulsDigestData hash = NulsDigestData.calcDigestData(new byte[20]);

        boolean success = agentStorageService.delete(hash);

        assert(success);

        AgentPo agentPo = agentStorageService.get(hash);

        assertNull(agentPo);
    }

    @Test
    public void testList() {
        assertNotNull(agentStorageService);

        testSave();

        NulsDigestData hash = NulsDigestData.calcDigestData(new byte[22]);

        AgentPo agentPo = new AgentPo();
        agentPo.setAgentAddress(new byte[22]);
        agentPo.setHash(hash);

        boolean success = agentStorageService.save(agentPo);

        assert(success);

        List<AgentPo> list = agentStorageService.getList();

        assertEquals(list.size(), 2);

        NulsDigestData hash1 = NulsDigestData.calcDigestData(new byte[20]);

        assertEquals(hash, list.get(0).getHash());
        assertEquals(hash1, list.get(1).getHash());

        int size = agentStorageService.size();
        assertEquals(size, 2);

        success = agentStorageService.delete(hash);
        assert(success);

        success = agentStorageService.delete(hash1);
        assert(success);
    }

    @Test
    public void testRepeatSave() {

        int i = 0;
        while(true) {
            testSave();
            testSave();
            testSave();
            testSave();
            i++;
            if(i > 100) {
                break;
            }
        }

        List<AgentPo> list = agentStorageService.getList();

        assertEquals(list.size(), 1);
    }
}