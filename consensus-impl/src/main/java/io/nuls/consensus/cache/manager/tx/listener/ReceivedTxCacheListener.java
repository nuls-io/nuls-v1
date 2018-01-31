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
package io.nuls.consensus.cache.manager.tx.listener;

import io.nuls.cache.entity.CacheListenerItem;
import io.nuls.cache.listener.intf.NulsCacheListener;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.TxStatusEnum;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.log.Log;
import io.nuls.ledger.service.intf.LedgerService;

/**
 * @author Niels
 * @date 2018/1/24
 */
public class ReceivedTxCacheListener implements NulsCacheListener<String, Transaction> {

    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);

    @Override
    public void onCreate(CacheListenerItem<String, Transaction> item) {
    }

    @Override
    public void onEvict(CacheListenerItem<String, Transaction> item) {
        rollbackTx(item.getOldValue());
    }

    private void rollbackTx(Transaction tx) {
        if (tx.getStatus() == TxStatusEnum.CACHED) {
            return;
        }
        try {
            ledgerService.rollbackTx(tx);
        } catch (NulsException e) {
            Log.error(e);
        }
    }

    @Override
    public void onRemove(CacheListenerItem<String, Transaction> item) {
    }

    @Override
    public void onUpdate(CacheListenerItem<String, Transaction> item) {
    }

    @Override
    public void onExpire(CacheListenerItem<String, Transaction> item) {
        rollbackTx(item.getOldValue());
    }
}
