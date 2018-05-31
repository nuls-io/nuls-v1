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

package io.nuls.consensus.poc.process;

import io.nuls.consensus.poc.BaseTest;
import io.nuls.consensus.poc.protocol.constant.PocConsensusProtocolConstant;
import io.nuls.consensus.poc.protocol.entity.Agent;
import io.nuls.consensus.poc.protocol.tx.CreateAgentTransaction;
import io.nuls.consensus.poc.tx.processor.CreateAgentTxProcessor;
import io.nuls.core.tools.crypto.ECKey;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.validate.ValidateResult;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by ln on 2018/5/10.
 */
public class RegisterAgentProcessTest extends BaseTest {

    private CreateAgentTxProcessor registerAgentProcess;

    private CreateAgentTransaction tx = newTx();

    @Before
    public void init() {
        registerAgentProcess = NulsContext.getServiceBean(CreateAgentTxProcessor.class);
    }

    @Test
    public void testOnRollback() {
        assertNotNull(tx);
        assertNotNull(registerAgentProcess);

        testOnCommit();
        Result result = registerAgentProcess.onRollback(tx, null);
        assert (result.isSuccess());
    }

    @Test
    public void testOnCommit() {
        assertNotNull(tx);
        assertNotNull(registerAgentProcess);

        Result result = registerAgentProcess.onCommit(tx, null);
        assert (result.isSuccess());
    }

    @Test
    public void testConflictDetect() {
        assertNotNull(tx);
        assertNotNull(registerAgentProcess);

        List<Transaction> list = new ArrayList<>();

        ValidateResult result = registerAgentProcess.conflictDetect(list);
        assertTrue(result.isSuccess());

        list.add(tx);

        result = registerAgentProcess.conflictDetect(list);
        assertTrue(result.isSuccess());

        list.add(newTx());
        result = registerAgentProcess.conflictDetect(list);
        assertTrue(result.isSuccess());

        list.add(tx);

        result = registerAgentProcess.conflictDetect(list);
        assertFalse(result.isSuccess());
    }

    private CreateAgentTransaction newTx() {
        CreateAgentTransaction tx = new CreateAgentTransaction();

        Agent agent = new Agent();

        tx.setTxData(agent);

        byte[] address = AddressTool.getAddress(ecKey.getPubKey());
        byte[] address1 = AddressTool.getAddress(new ECKey().getPubKey());
        byte[] address2 = AddressTool.getAddress(new ECKey().getPubKey());
        agent.setRewardAddress(address);
        agent.setPackingAddress(address1);
        agent.setAgentAddress(address2);
        agent.setAgentName("test".getBytes());
        agent.setDeposit(PocConsensusProtocolConstant.AGENT_DEPOSIT_LOWER_LIMIT);

        tx.setTime(System.currentTimeMillis());

        return tx;
    }
}