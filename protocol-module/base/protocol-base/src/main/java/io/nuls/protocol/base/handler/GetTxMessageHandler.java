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

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;
import io.nuls.message.bus.handler.AbstractMessageHandler;
import io.nuls.message.bus.service.MessageBusService;
import io.nuls.network.model.Node;
import io.nuls.protocol.cache.TemporaryCacheManager;
import io.nuls.protocol.constant.MessageDataType;
import io.nuls.protocol.message.*;
import io.nuls.protocol.model.NotFound;

/**
 * @author facjas
 * @date 2017/11/16
 */
public class GetTxMessageHandler extends AbstractMessageHandler<GetTxMessage> {

    private TemporaryCacheManager cacheManager = TemporaryCacheManager.getInstance();
    private MessageBusService messageBusService = NulsContext.getServiceBean(MessageBusService.class);

    @Override
    public void onMessage(GetTxMessage message, Node fromNode) {
        if (message == null || fromNode == null || null == message.getMsgBody()) {
            return;
        }
        Transaction tx = cacheManager.getTx(message.getMsgBody());
        if (null == tx) {
            sendNotFound(message.getMsgBody(), fromNode);
        }
        TransactionMessage txMessage = new TransactionMessage();
        txMessage.setMsgBody(tx);
        messageBusService.sendToNode(txMessage, fromNode, true);
    }

    private void sendNotFound(NulsDigestData hash, Node fromNode) {
        NotFoundMessage event = new NotFoundMessage();
        NotFound data = new NotFound(MessageDataType.TRANSACTION, hash);
        event.setMsgBody(data);
        Result result = this.messageBusService.sendToNode(event, fromNode, true);
        if (result.isFailed()) {
            Log.warn("send not found failed:" + fromNode.getId() + ", hash:" + hash);
        }
    }

}
