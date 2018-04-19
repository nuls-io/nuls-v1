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
package io.nuls.ledger.entity.listener;

import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.Log;
import io.nuls.core.validate.ValidateResult;
import io.nuls.db.transactional.annotation.DbSession;
import io.nuls.db.transactional.annotation.PROPAGATION;
import io.nuls.ledger.entity.tx.AbstractCoinTransaction;
import io.nuls.protocol.model.Block;
import io.nuls.protocol.model.Transaction;
import io.nuls.protocol.service.intf.TransactionService;

import java.util.List;

/**
 * @author Niels
 * @date 2017/12/21
 */
@DbSession(transactional = PROPAGATION.NONE)
public class CoinDataTxService implements TransactionService<AbstractCoinTransaction> {

    @Override
    @DbSession
    public void onRollback(AbstractCoinTransaction tx, Block block) {
        tx.getCoinDataProvider().rollback(tx.getCoinData(), tx);
    }

    @Override
    @DbSession
    public void onCommit(AbstractCoinTransaction tx, Block block) {
        try {
            tx.getCoinDataProvider().save(tx.getCoinData(), tx);
        } catch (NulsException e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
    }

    @Override
    public ValidateResult conflictDetect(AbstractCoinTransaction tx, List<Transaction> txList) {
        // todo auto-generated method stub(niels)
        return null;
    }

}
