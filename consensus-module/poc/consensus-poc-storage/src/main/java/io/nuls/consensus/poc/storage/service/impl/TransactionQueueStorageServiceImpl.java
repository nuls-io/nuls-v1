/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

package io.nuls.consensus.poc.storage.service.impl;

import io.nuls.consensus.poc.storage.service.TransactionQueueStorageService;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.TransactionManager;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author: Niels Wang
 * @date: 2018/7/8
 */
@Component
public class TransactionQueueStorageServiceImpl implements TransactionQueueStorageService {

    private LinkedBlockingQueue<byte[]> queue = new LinkedBlockingQueue(10000000);

    public TransactionQueueStorageServiceImpl() throws Exception {
    }

    @Override
    public boolean putTx(Transaction tx) {
        try {
            queue.offer(tx.serialize());
            return true;
        } catch (IOException e) {
            Log.error(e);
        }
        return false;
    }

    @Override
    public Transaction pollTx() {
        byte[] bytes = queue.poll();
        if (null == bytes) {
            return null;
        }
        try {
            return TransactionManager.getInstance(new NulsByteBuffer(bytes));
        } catch (Exception e) {
            Log.error(e);
        }
        return null;
    }
}
