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
 *
 */
package io.nuls.utxo.accounts.task;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.utxo.accounts.locker.Lockers;
import io.nuls.utxo.accounts.service.UtxoAccountsService;
import io.nuls.utxo.accounts.storage.service.UtxoAccountsStorageService;

@Component
public class UtxoAccountsThread implements Runnable {
    @Autowired
    private UtxoAccountsService utxoAccountsService;
    @Autowired
    private UtxoAccountsStorageService utxoAccountsStorageService;

    @Override
    public void run() {
        Lockers.SYN_UTXO_ACCOUNTS_LOCK.lock();
        try {
            long hadSynBlockHeight = utxoAccountsStorageService.getHadSynBlockHeight();
            long end = NulsContext.getInstance().getBestHeight();
            for (long i = hadSynBlockHeight + 1; i <= end; i++) {
                if (!utxoAccountsService.synBlock(i)) {
                    Log.error("utxoAccounts block syn fail!");
                    break;
                }
            }
        } catch (Exception e) {
            Log.error(e);
        } finally {
            Lockers.SYN_UTXO_ACCOUNTS_LOCK.unlock();
        }
    }

}
