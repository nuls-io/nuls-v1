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
import io.nuls.ledger.service.LedgerService;
import io.nuls.message.bus.handler.AbstractMessageHandler;
import io.nuls.network.entity.Node;
import io.nuls.protocol.cache.TemporaryCacheManager;
import io.nuls.protocol.constant.NotFoundType;
import io.nuls.protocol.message.GetTxGroupRequest;
import io.nuls.protocol.message.NotFoundMessage;
import io.nuls.protocol.message.TxGroupMessage;
import io.nuls.protocol.model.NotFound;
import io.nuls.protocol.model.TxGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * @author facjas
 * @date 2017/11/16
 */
public class GetTxGroupHandler extends AbstractMessageHandler<GetTxGroupRequest> {

    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);
    private TemporaryCacheManager temporaryCacheManager = TemporaryCacheManager.getInstance();

    @Override
    public void onMessage(GetTxGroupRequest message, Node fromNode) {
//        GetTxGroupParam eventBody = message.getMsgBody();

        if (message.getMsgBody().getTxHashList() == null || message.getMsgBody().getTxHashList().size() > 10000) {
            return;
        }

        TxGroupMessage txGroupMessage = new TxGroupMessage();
        TxGroup txGroup = new TxGroup();
        List<Transaction> txList = new ArrayList<>();

        for (NulsDigestData hash : message.getMsgBody().getTxHashList()) {
            Transaction tx = temporaryCacheManager.getTx(hash);
            if (tx == null) {
                tx = ledgerService.getTx(hash);
            }
            if (tx != null) {
                txList.add(tx);
            } else {
                this.sendNotFound(message.getHash(), fromNode);
            }
        }
        if (txList.isEmpty()) {
            Log.error("ASK:{}, {}", fromNode, message.getMsgBody().getTxHashList().get(0));
            return;
        }

        txGroup.setTxList(txList);
        txGroup.setRequestHash(message.getHash());
        txGroupMessage.setMsgBody(txGroup);
        messageBusService.sendToNode(txGroupMessage, fromNode, true);
    }

    private void sendNotFound(NulsDigestData hash, Node fromNode) {
        NotFoundMessage event = new NotFoundMessage();
        NotFound data = new NotFound(NotFoundType.TRANSACTION, hash);
        event.setMsgBody(data);
        Result result = this.messageBusService.sendToNode(event, fromNode, true);
        if (result.isFailed()) {
            Log.warn("send not found failed:" + fromNode.getId() + ", hash:" + hash.getDigestHex());
        }
    }

}
