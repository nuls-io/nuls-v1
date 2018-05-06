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
package io.nuls.consensus.tx;

import io.nuls.consensus.constant.ConsensusTransactionConstant;
import io.nuls.consensus.entity.Agent;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.CoinData;
import io.nuls.kernel.model.Transaction;
import io.nuls.protocol.constant.TransactionConstant;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class RegisterAgentTransaction extends Transaction<Agent> {

    public RegisterAgentTransaction() {
        super(TransactionConstant.TX_TYPE_REGISTER_AGENT);
        this.initValidator();
    }

    public RegisterAgentTransaction(CoinData coinData) throws NulsException {
        super(ConsensusTransactionConstant.TX_TYPE_REGISTER_AGENT);
        this.initValidator();
        this.setCoinData(coinData);
    }

    private void initValidator() {

    }

    @Override
    public Agent parseTxData(byte[] bytes) {
        Agent agent = new Agent();
        agent.parse(bytes);
        return agent;
    }
}
