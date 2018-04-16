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
package io.nuls.protocol.base.event.handler;

import io.nuls.core.chain.entity.*;
import io.nuls.core.context.NulsContext;
import io.nuls.event.bus.handler.AbstractEventHandler;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.network.service.NetworkService;
import io.nuls.poc.service.intf.ConsensusService;
import io.nuls.protocol.base.cache.manager.block.TemporaryCacheManager;
import io.nuls.protocol.base.event.notice.AssembledBlockNotice;
import io.nuls.protocol.base.utils.ConsensusTool;
import io.nuls.protocol.entity.GetTxGroupParam;
import io.nuls.protocol.entity.TxGroup;
import io.nuls.protocol.event.GetTxGroupRequest;
import io.nuls.protocol.event.SmallBlockEvent;
import io.nuls.protocol.event.TxGroupEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author facjas
 * @date 2017/11/16
 */
public class TxGroupHandler extends AbstractEventHandler<TxGroupEvent> {

    private TemporaryCacheManager temporaryCacheManager = TemporaryCacheManager.getInstance();
    private NetworkService networkService = NulsContext.getServiceBean(NetworkService.class);
    private EventBroadcaster eventBroadcaster = NulsContext.getServiceBean(EventBroadcaster.class);
    private ConsensusService consensusService = NulsContext.getServiceBean(ConsensusService.class);

    @Override
    public void onEvent(TxGroupEvent event, String fromId) {
        TxGroup txGroup = event.getEventBody();

        SmallBlock smallBlock = temporaryCacheManager.getSmallBlock(txGroup.getBlockHash().getDigestHex());
        if (smallBlock == null) {
            return;
        }
        BlockHeader header = smallBlock.getHeader();

        Map<String, Transaction> txMap = new HashMap<>();
        for (Transaction tx : smallBlock.getSubTxList()) {
            txMap.put(tx.getHash().getDigestHex(), tx);
        }
        List<NulsDigestData> needHashList = new ArrayList<>();
        for (NulsDigestData hash : smallBlock.getTxHashList()) {
            Transaction tx = txGroup.getTx(hash.getDigestHex());
//            if (null == tx) {
//                tx = this.receivedTxCacheManager.getTx(hash);
//            }
//            if (null == tx) {
//                tx = orphanTxCacheManager.getTx(hash);
//            }
//            if (null == tx) {
//                tx = confirmingTxCacheManager.getTx(hash);
//            }
            if (null == tx && txMap.get(hash) == null) {
                needHashList.add(hash);
                continue;
            }
            txMap.put(tx.getHash().getDigestHex(), tx);
        }
        if (!needHashList.isEmpty()) {
            GetTxGroupRequest request = new GetTxGroupRequest();
            GetTxGroupParam param = new GetTxGroupParam();
            param.setBlockHash(header.getHash());
            for (NulsDigestData hash : needHashList) {
                param.addHash(hash);
            }
            request.setEventBody(param);
            this.eventBroadcaster.sendToNode(request, fromId);
            return;
        }

        Block block = ConsensusTool.assemblyBlock(header, txMap, smallBlock.getTxHashList());

//        boolean needForward = blockManager.addBlock(block, true, fromId);
        boolean needForward = consensusService.newBlock(block, networkService.getNode(fromId));
        if(needForward) {
            SmallBlockEvent newBlockEvent = new SmallBlockEvent();
            newBlockEvent.setEventBody(smallBlock);
            List<String> addressList = eventBroadcaster.broadcastHashAndCache(newBlockEvent, fromId);
//            for (String address : addressList) {
//                BlockLog.debug("forward blockHeader:(" + address + ")" + header.getHeight() + ", hash:" + header.getHash() + ", preHash:" + header.getPreHash() + ", packing:" + Address.fromHashs(header.getPackingAddress()));
//            }
        }
        AssembledBlockNotice notice = new AssembledBlockNotice();
        notice.setEventBody(header);
        eventBroadcaster.publishToLocal(notice);

    }
}
