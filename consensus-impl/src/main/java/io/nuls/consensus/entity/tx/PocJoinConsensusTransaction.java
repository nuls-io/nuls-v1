/**
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
package io.nuls.consensus.entity.tx;

import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.ConsensusDepositImpl;
import io.nuls.consensus.entity.member.Deposit;
import io.nuls.consensus.entity.validator.consensus.DelegateCountValidator;
import io.nuls.consensus.entity.validator.consensus.DelegateDepositValidator;
import io.nuls.consensus.entity.validator.tx.AllreadyJoinConsensusValidator;
import io.nuls.consensus.entity.validator.tx.CreditThresholdValidator;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.entity.tx.LockNulsTransaction;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class PocJoinConsensusTransaction extends LockNulsTransaction<Consensus<Deposit>> {
    public PocJoinConsensusTransaction() {
        super(TransactionConstant.TX_TYPE_JOIN_CONSENSUS);
        this.initValidator();
    }

    public PocJoinConsensusTransaction(CoinTransferData lockData, String password) throws NulsException {
        super(TransactionConstant.TX_TYPE_JOIN_CONSENSUS, lockData, password);
        this.initValidator();
    }

    private void initValidator() {
        this.registerValidator(AllreadyJoinConsensusValidator.getInstance());
        this.registerValidator(CreditThresholdValidator.getInstance());
        this.registerValidator(DelegateCountValidator.getInstance());
        this.registerValidator(DelegateDepositValidator.getInstance());
    }

    @Override
    public Consensus<Deposit> parseTxData(NulsByteBuffer byteBuffer) throws NulsException {
        Consensus<Deposit> consensus = byteBuffer.readNulsData(new ConsensusDepositImpl());
        return consensus;
    }
}
