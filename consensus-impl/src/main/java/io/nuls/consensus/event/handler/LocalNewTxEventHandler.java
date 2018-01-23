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
package io.nuls.consensus.event.handler;

import io.nuls.consensus.cache.manager.tx.ReceivedTxCacheManager;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.SeverityLevelEnum;
import io.nuls.core.context.NulsContext;
import io.nuls.core.validate.ValidateResult;
import io.nuls.event.bus.handler.AbstractEventHandler;
import io.nuls.ledger.event.TransactionEvent;
import io.nuls.network.service.NetworkService;

/**
 * @author Niels
 * @date 2018/1/8
 */
public class LocalNewTxEventHandler extends AbstractEventHandler<TransactionEvent> {

    private static LocalNewTxEventHandler INSTANCE = new LocalNewTxEventHandler();

    private ReceivedTxCacheManager cacheManager = ReceivedTxCacheManager.getInstance();

    private LocalNewTxEventHandler() {
    }

    public static LocalNewTxEventHandler getInstance() {
        return INSTANCE;
    }
    //todo 如果是自己的，超时需要回滚
    @Override
    public void onEvent(TransactionEvent event, String fromId) {

    }
}
