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
package io.nuls.protocol.base.handler;

import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;
import io.nuls.message.bus.handler.AbstractMessageHandler;
import io.nuls.network.model.Node;
import io.nuls.protocol.base.cache.ProtocolCacheHandler;
import io.nuls.protocol.base.download.tx.TransactionContainer;
import io.nuls.protocol.base.download.tx.TransactionDownloadProcessor;
import io.nuls.protocol.cache.TemporaryCacheManager;
import io.nuls.protocol.message.ForwardTxMessage;
import io.nuls.protocol.message.GetTxMessage;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * @author facjas
 */
public class ForwardTxMessageHandler extends AbstractMessageHandler<ForwardTxMessage> {

    private TemporaryCacheManager cacheManager = TemporaryCacheManager.getInstance();

    private TransactionDownloadProcessor txDownloadProcessor = TransactionDownloadProcessor.getInstance();

    private Set<NulsDigestData> set = new HashSet<>();

    @Override
    public void onMessage(ForwardTxMessage message, Node fromNode) {
        if (message == null || fromNode == null || null == message.getMsgBody()) {
            return;
        }
        NulsDigestData hash = message.getMsgBody();
//        InventoryFilter inventoryFilter = ProtocolCacheHandler.TX_FILTER;
//        boolean constains = inventoryFilter.contains(hash.getDigestBytes());
        if (!set.add(hash)) {
//            Log.error("重复交易：：：：" + hash.getDigestHex());
            return;
        }
        if (set.size() >= 100000) {
            set.clear();
            set.add(hash);
        }
        GetTxMessage getTxMessage = new GetTxMessage();
        getTxMessage.setMsgBody(hash);
        CompletableFuture<Transaction> future = ProtocolCacheHandler.addGetTxRequest(hash);
        Result result = messageBusService.sendToNode(getTxMessage, fromNode, false);
        if (result.isFailed()) {
            ProtocolCacheHandler.removeTxFuture(hash);
            return;
        }
//        inventoryFilter.insert(hash.getDigestBytes());
        txDownloadProcessor.offer(new TransactionContainer(fromNode, future,hash));


    }

}
