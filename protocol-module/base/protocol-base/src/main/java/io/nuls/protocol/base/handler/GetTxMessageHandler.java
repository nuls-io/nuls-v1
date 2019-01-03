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
package io.nuls.protocol.base.handler;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;
import io.nuls.message.bus.handler.AbstractMessageHandler;
import io.nuls.message.bus.service.MessageBusService;
import io.nuls.network.model.Node;
import io.nuls.protocol.message.GetTxMessage;
import io.nuls.protocol.message.TransactionMessage;
import io.nuls.protocol.service.TransactionService;

/**
 * @author facjas
 */
public class GetTxMessageHandler extends AbstractMessageHandler<GetTxMessage> {

    private MessageBusService messageBusService = NulsContext.getServiceBean(MessageBusService.class);
    private TransactionService transactionService = NulsContext.getServiceBean(TransactionService.class);

    @Override
    public void onMessage(GetTxMessage message, Node fromNode) {
        if (message == null || fromNode == null || null == message.getMsgBody()) {
            return;
        }
        Transaction tx = transactionService.getTx(message.getMsgBody());
        if (null == tx) {
            return;
        }

        TransactionMessage txMessage = new TransactionMessage();
        txMessage.setMsgBody(tx);
        Result result = messageBusService.sendToNode(txMessage, fromNode, true);
        if (!result.isSuccess()) {
            Log.error("send error to node : " + fromNode.getId());
        }
    }

}
