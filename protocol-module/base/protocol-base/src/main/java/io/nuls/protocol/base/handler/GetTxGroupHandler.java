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
import io.nuls.network.model.Node;
import io.nuls.protocol.constant.MessageDataType;
import io.nuls.protocol.message.GetTxGroupRequest;
import io.nuls.protocol.message.NotFoundMessage;
import io.nuls.protocol.message.TxGroupMessage;
import io.nuls.protocol.model.GetTxGroupParam;
import io.nuls.protocol.model.NotFound;
import io.nuls.protocol.model.TxGroup;
import io.nuls.protocol.service.TransactionService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author facjas
 */
public class GetTxGroupHandler extends AbstractMessageHandler<GetTxGroupRequest> {

    private TransactionService transactionService = NulsContext.getServiceBean(TransactionService.class);

    @Override
    public void onMessage(GetTxGroupRequest message, Node fromNode) {

        if(message == null || fromNode == null) {
            return;
        }

        GetTxGroupParam getTxGroupParam = message.getMsgBody();
        if (getTxGroupParam == null || getTxGroupParam.getTxHashList() == null || getTxGroupParam.getTxHashList().size() > 10000) {
            return;
        }
        NulsDigestData requestHash = null;
        try {
            requestHash = NulsDigestData.calcDigestData(getTxGroupParam.serialize());
        } catch (IOException e) {
            Log.error(e);
            return;
        }

        TxGroupMessage txGroupMessage = new TxGroupMessage();
        TxGroup txGroup = new TxGroup();
        List<Transaction> txList = new ArrayList<>();

        for (NulsDigestData hash : getTxGroupParam.getTxHashList()) {
            Transaction tx = transactionService.getTx(hash);
            if (tx != null) {
                txList.add(tx);
            } else {
                this.sendNotFound(requestHash, fromNode);
                return;
            }
        }
        if (txList.isEmpty()) {
            Log.error("ASK:{}, {}", fromNode, getTxGroupParam.getTxHashList().get(0));
            return;
        }

        txGroup.setTxList(txList);
        txGroup.setRequestHash(requestHash);

        txGroupMessage.setMsgBody(txGroup);
        messageBusService.sendToNode(txGroupMessage, fromNode, true);
    }

    private void sendNotFound(NulsDigestData hash, Node fromNode) {
        NotFoundMessage event = new NotFoundMessage();
        NotFound data = new NotFound(MessageDataType.TRANSACTIONS, hash);
        event.setMsgBody(data);
        Result result = this.messageBusService.sendToNode(event, fromNode, true);
        if (result.isFailed()) {
            Log.warn("send not found failed:" + fromNode.getId() + ", hash:" + hash);
        }
    }

}
