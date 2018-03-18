/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
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
import io.nuls.core.tx.serivce.TransactionService;
import io.nuls.core.utils.log.Log;
import io.nuls.ledger.entity.tx.AbstractCoinTransaction;

/**
 * @author Niels
 * @date 2017/12/21
 */
public class CoinDataTxService implements TransactionService<AbstractCoinTransaction> {
    private static final CoinDataTxService INSTANCE = new CoinDataTxService();

    private CoinDataTxService() {
    }

    @Override
    public void onRollback(AbstractCoinTransaction tx) {
        tx.getCoinDataProvider().rollback(tx.getCoinData(), tx);
    }

    @Override
    public void onCommit(AbstractCoinTransaction tx) {
        try {
            tx.getCoinDataProvider().save(tx.getCoinData(), tx);
        } catch (NulsException e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
    }

    @Override
    public void onApproval(AbstractCoinTransaction tx) {
        try {
            tx.getCoinDataProvider().approve(tx.getCoinData(), tx);
        } catch (NulsException e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
    }

    public static CoinDataTxService getInstance() {
        return INSTANCE;
    }
}
