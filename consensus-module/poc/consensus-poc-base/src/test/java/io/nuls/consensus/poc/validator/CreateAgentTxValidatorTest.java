/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2019 nuls.io
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

package io.nuls.consensus.poc.validator;

import io.nuls.consensus.poc.BaseTest;
import io.nuls.consensus.poc.protocol.constant.PocConsensusProtocolConstant;
import io.nuls.consensus.poc.protocol.entity.Agent;
import io.nuls.consensus.poc.protocol.tx.CreateAgentTransaction;
import io.nuls.consensus.poc.tx.validator.CreateAgentTxValidator;
import io.nuls.core.tools.crypto.ECKey;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.validate.ValidateResult;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by ln on 2018/5/10.
 */
public class CreateAgentTxValidatorTest extends BaseTest {

    @Test
    public void test() {
        CreateAgentTransaction tx = new CreateAgentTransaction();

        CreateAgentTxValidator validator = new CreateAgentTxValidator();

        ValidateResult result = validator.validate(tx);
        assertFalse(result.isSuccess());

        Agent agent = new Agent();

        tx.setTxData(agent);

        result = validator.validate(tx);
        assertFalse(result.isSuccess());

        byte[] address = AddressTool.getAddress(ecKey.getPubKey());
        byte[] address1 = AddressTool.getAddress(new ECKey().getPubKey());
        byte[] address2 = AddressTool.getAddress(new ECKey().getPubKey());
        agent.setRewardAddress(address);
        agent.setPackingAddress(address1);
        agent.setAgentAddress(address2);

        result = validator.validate(tx);
        assertFalse(result.isSuccess());

        agent.setDeposit(PocConsensusProtocolConstant.AGENT_DEPOSIT_LOWER_LIMIT);

        tx.setTime(System.currentTimeMillis());

        result = validator.validate(tx);
        assertTrue(result.isSuccess());
    }
}