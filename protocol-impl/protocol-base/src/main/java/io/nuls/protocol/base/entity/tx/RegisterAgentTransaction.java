/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.protocol.base.entity.tx;

import io.nuls.protocol.entity.Consensus;
import io.nuls.protocol.base.entity.member.Agent;
import io.nuls.protocol.base.entity.validator.tx.AccountCreditValidator;
import io.nuls.protocol.base.entity.validator.tx.AgentCountValidator;
import io.nuls.protocol.base.entity.validator.tx.AgentDepositValidator;
import io.nuls.protocol.base.entity.validator.tx.CommissionRateValidator;
import io.nuls.protocol.base.entity.validator.tx.RegisterAgentFieldValidator;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.entity.tx.LockNulsTransaction;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class RegisterAgentTransaction extends LockNulsTransaction<Consensus<Agent>> {

    public RegisterAgentTransaction() {
        super(TransactionConstant.TX_TYPE_REGISTER_AGENT);
        this.initValidator();
    }

    public RegisterAgentTransaction(CoinTransferData lockData, String password) throws NulsException {
        super(TransactionConstant.TX_TYPE_REGISTER_AGENT, lockData, password);
        this.initValidator();
    }

    private void initValidator() {
        this.registerValidator(new RegisterAgentFieldValidator());
        this.registerValidator(new CommissionRateValidator());
        this.registerValidator(new AccountCreditValidator());
        this.registerValidator(new AgentDepositValidator());
        this.registerValidator(new AgentCountValidator());
    }

    @Override
    public Consensus<Agent> parseTxData(NulsByteBuffer byteBuffer) throws NulsException {
        Consensus<Agent> consensus = byteBuffer.readNulsData(new ConsensusAgentImpl());
        return consensus;
    }

}
