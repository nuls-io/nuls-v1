/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.consensus.event.handler;

import io.nuls.account.entity.Address;
import io.nuls.consensus.cache.manager.block.TemporaryCacheManager;
import io.nuls.consensus.cache.manager.tx.ConfirmingTxCacheManager;
import io.nuls.consensus.cache.manager.tx.OrphanTxCacheManager;
import io.nuls.consensus.cache.manager.tx.ReceivedTxCacheManager;
import io.nuls.consensus.entity.GetTxGroupParam;
import io.nuls.consensus.entity.TxGroup;
import io.nuls.consensus.event.GetTxGroupRequest;
import io.nuls.consensus.event.SmallBlockEvent;
import io.nuls.consensus.event.TxGroupEvent;
import io.nuls.consensus.event.notice.AssembledBlockNotice;
import io.nuls.consensus.manager.BlockManager;
import io.nuls.consensus.utils.ConsensusTool;
import io.nuls.core.chain.entity.*;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.log.BlockLog;
import io.nuls.event.bus.handler.AbstractEventHandler;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.network.service.NetworkService;

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
    private ReceivedTxCacheManager receivedTxCacheManager = ReceivedTxCacheManager.getInstance();
    private ConfirmingTxCacheManager confirmingTxCacheManager = ConfirmingTxCacheManager.getInstance();
    private OrphanTxCacheManager orphanTxCacheManager = OrphanTxCacheManager.getInstance();
    private NetworkService networkService = NulsContext.getServiceBean(NetworkService.class);
    private EventBroadcaster eventBroadcaster = NulsContext.getServiceBean(EventBroadcaster.class);
    private BlockManager blockManager = BlockManager.getInstance();

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
            if (null == tx) {
                tx = this.receivedTxCacheManager.getTx(hash);
            }
            if (null == tx) {
                tx = orphanTxCacheManager.getTx(hash);
            }
            if (null == tx) {
                tx = confirmingTxCacheManager.getTx(hash);
            }
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
        boolean needForward = blockManager.addBlock(block, true, fromId);
        if(needForward) {
            SmallBlockEvent newBlockEvent = new SmallBlockEvent();
            newBlockEvent.setEventBody(smallBlock);
            List<String> addressList = eventBroadcaster.broadcastHashAndCache(newBlockEvent, false, fromId);
            for (String address : addressList) {
                BlockLog.debug("forward blockHeader:(" + address + ")" + header.getHeight() + ", hash:" + header.getHash() + ", preHash:" + header.getPreHash() + ", packing:" + Address.fromHashs(header.getPackingAddress()));
            }
        }
        AssembledBlockNotice notice = new AssembledBlockNotice();
        notice.setEventBody(header);
        eventBroadcaster.publishToLocal(notice);

    }
}
