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

import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.entity.Deposit;
import io.nuls.kernel.model.Transaction;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class JoinConsensusTransaction extends Transaction<Deposit> {

    public JoinConsensusTransaction() {
        super(ConsensusConstant.TX_TYPE_JOIN_CONSENSUS);
        this.initValidator();
    }

    private void initValidator() {
        // TODO
    }

    public JoinConsensusTransaction clone() {
        JoinConsensusTransaction tx = new JoinConsensusTransaction();
        tx.parse(serialize());
        tx.setBlockHeight(blockHeight);
        tx.setIndex(index);
        tx.setStatus(status);
        tx.setHash(hash);
        tx.setTransferType(transferType);
        tx.setSize(size);
        tx.setMine(isMine);

        Deposit deposit = tx.getTxData();
        deposit.setBlockHeight(txData.getBlockHeight());
        deposit.setDelHeight(txData.getDelHeight());
        deposit.setTime(txData.getTime());
        deposit.setTxHash(txData.getTxHash());
        deposit.setStatus(txData.getStatus());

        return tx;
    }
}
