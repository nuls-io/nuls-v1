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

import io.nuls.consensus.entity.TxGroup;
import io.nuls.consensus.entity.GetTxGroupParam;
import io.nuls.consensus.event.GetTxGroupRequest;
import io.nuls.consensus.event.TxGroupEvent;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.handler.AbstractEventHandler;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.ledger.service.intf.LedgerService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author facjas
 * @date 2017/11/16
 */
public class GetTxGroupHandler extends AbstractEventHandler<GetTxGroupRequest> {

    private EventBroadcaster eventBroadcaster = NulsContext.getServiceBean(EventBroadcaster.class);
    private BlockService blockService =NulsContext.getServiceBean(BlockService.class);

    @Override
    public void onEvent(GetTxGroupRequest event, String fromId) {
        GetTxGroupParam eventBody = event.getEventBody();
        Block block = blockService.getBlock(eventBody.getBlockHash().getDigestHex());
        if (null == block) {
            return;
        }
        TxGroupEvent txGroupEvent = new TxGroupEvent();
        TxGroup txGroup = new TxGroup();
        txGroup.setBlockHash(block.getHeader().getHash());
        List<Transaction> txList = getTxList(block,eventBody.getTxHashList());
        txGroup.setTxList(txList);
        txGroupEvent.setEventBody(txGroup);
        eventBroadcaster.sendToNode(txGroupEvent, fromId);
    }

    private List<Transaction> getTxList(Block block, List<NulsDigestData> txHashList) {
        List<Transaction> txList = new ArrayList<>();
        Map<String, Integer> allTxMap = new HashMap<>();
        for (int i = 0; i < block.getHeader().getTxCount(); i++) {
            Transaction tx = block.getTxs().get(i);
            allTxMap.put(tx.getHash().getDigestHex(), i);
        }
        for (NulsDigestData hash : txHashList) {
            txList.add(block.getTxs().get(allTxMap.get(hash.getDigestHex())));
        }
        if (txList.size() != txHashList.size()) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR);
        }
        return txList;
    }
}
