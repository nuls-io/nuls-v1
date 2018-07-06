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

    private TransactionDownloadProcessor txDownloadProcessor = TransactionDownloadProcessor.getInstance();
    private TemporaryCacheManager cacheManager = TemporaryCacheManager.getInstance();

    private Set<NulsDigestData> set = new HashSet<>();
    private Set<NulsDigestData> tempSet = new HashSet<>();

    @Override
    public void onMessage(ForwardTxMessage message, Node fromNode) {
        if (message == null || fromNode == null || !fromNode.isHandShake() || null == message.getMsgBody()) {
            return;
        }
        NulsDigestData hash = message.getMsgBody();
        if (!set.add(hash)) {
            return;
        } else if (cacheManager.containsTx(hash)) {
            return;
        }
        tempSet.add(hash);
        if (set.size() >= 100000) {
            set.clear();
            set.addAll(tempSet);
            tempSet.clear();
        }
        if (tempSet.size() == 50000) {
            tempSet.clear();
        }
        GetTxMessage getTxMessage = new GetTxMessage();
        getTxMessage.setMsgBody(hash);
        Result result = messageBusService.sendToNode(getTxMessage, fromNode, true);
        if (result.isFailed()) {
            set.remove(hash);
            return;
        }
    }

}
