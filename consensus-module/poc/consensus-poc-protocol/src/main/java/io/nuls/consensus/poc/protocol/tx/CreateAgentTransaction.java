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
package io.nuls.consensus.poc.protocol.tx;

import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.poc.protocol.entity.Agent;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.utils.NulsByteBuffer;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class CreateAgentTransaction extends Transaction<Agent> {

    public CreateAgentTransaction() {
        super(ConsensusConstant.TX_TYPE_REGISTER_AGENT);
    }

    @Override
    public CreateAgentTransaction clone() {
        CreateAgentTransaction tx = new CreateAgentTransaction();
        try {
            tx.parse(serialize());
        } catch (Exception e) {
            throw new NulsRuntimeException(e);
        }
        tx.setBlockHeight(blockHeight);
        tx.setStatus(status);
        tx.setHash(hash);
        tx.setSize(size);

        Agent agent = tx.getTxData();
        agent.setBlockHeight(txData.getBlockHeight());
        agent.setDelHeight(txData.getDelHeight());
        agent.setTime(txData.getTime());
        agent.setTxHash(txData.getTxHash());
        agent.setStatus(txData.getStatus());
        agent.setTotalDeposit(txData.getTotalDeposit());
        agent.setCreditVal(txData.getCreditVal());

        return tx;
    }

    @Override
    protected Agent parseTxData(NulsByteBuffer byteBuffer) throws NulsException {
        return byteBuffer.readNulsData(new Agent());
    }
}
