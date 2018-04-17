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

package io.nuls.consensus.poc.process;

import io.nuls.consensus.poc.cache.TxMemoryPool;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.validate.ValidateResult;
import io.nuls.event.bus.service.intf.EventBroadcaster;

/**
 * Created by ln on 2018/4/13.
 */
public class TxProcess {

    private TxMemoryPool memoryPool;

    private EventBroadcaster eventBroadcaster = NulsContext.getServiceBean(EventBroadcaster.class);

    public TxProcess(TxMemoryPool memoryPool) {
        this.memoryPool = memoryPool;
    }


    public void process(Transaction tx) {

        ValidateResult result = tx.verify();

        if(result.isSuccess()) {
            memoryPool.add(tx, false);

            // Forward transaction
            // 转发交易
            TransactionEvent txEvent = new TransactionEvent();
            txEvent.setEventBody(tx);
            eventBroadcaster.broadcastHashAndCache(txEvent);

        } else if(result.isFailed() && ErrorCode.ORPHAN_TX == result.getErrorCode()) {
            memoryPool.add(tx, true);
        }
    }
}
