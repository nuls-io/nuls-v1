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
package io.nuls.consensus.poc.handler;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.event.bus.handler.AbstractEventHandler;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.event.GetTxGroupRequest;
import io.nuls.protocol.event.TxGroupEvent;
import io.nuls.protocol.event.entity.GetTxGroupParam;
import io.nuls.protocol.event.entity.TxGroup;
import io.nuls.protocol.model.Block;
import io.nuls.protocol.model.NulsDigestData;
import io.nuls.protocol.model.Transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * @author facjas
 * @date 2017/11/16
 */
public class GetTxGroupHandler extends AbstractEventHandler<GetTxGroupRequest> {

    private EventBroadcaster eventBroadcaster = NulsContext.getServiceBean(EventBroadcaster.class);
//    private BlockService blockService =NulsContext.getServiceBean(BlockService.class);
private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);

    @Override
    public void onEvent(GetTxGroupRequest event, String fromId) {
        GetTxGroupParam eventBody = event.getEventBody();

        TxGroupEvent txGroupEvent = new TxGroupEvent();
        TxGroup txGroup = new TxGroup();
        txGroup.setBlockHash(event.getEventBody().getBlockHash());
        List<Transaction> txList =new ArrayList<>();

        for(NulsDigestData hash:event.getEventBody().getTxHashList()){

            Transaction tx = ledgerService.getTx(hash);
            txList.add(tx);
        }

        txGroup.setTxList(txList);
        txGroupEvent.setEventBody(txGroup);
        eventBroadcaster.sendToNode(txGroupEvent, fromId);
    }

    private List<Transaction> getTxList(Block block, List<NulsDigestData> txHashList) {
        List<Transaction> txList = new ArrayList<>();

        for (Transaction tx : block.getTxs()) {
            if (txHashList.contains(tx.getHash())) {
                txList.add(tx);
            }
        }

        if (txList.size() != txHashList.size()) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR);
        }
        return txList;
    }
}
