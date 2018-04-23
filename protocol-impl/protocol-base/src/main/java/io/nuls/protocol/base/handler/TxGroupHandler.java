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
package io.nuls.protocol.base.handler;

import io.nuls.consensus.poc.protocol.event.notice.AssembledBlockNotice;
import io.nuls.consensus.poc.protocol.utils.ConsensusTool;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.utils.log.BlockLog;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.handler.AbstractEventHandler;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.network.service.NetworkService;
import io.nuls.poc.service.intf.ConsensusService;
import io.nuls.protocol.cache.TemporaryCacheManager;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.event.GetTxGroupRequest;
import io.nuls.protocol.event.SmallBlockEvent;
import io.nuls.protocol.event.TxGroupEvent;
import io.nuls.protocol.event.entity.GetTxGroupParam;
import io.nuls.protocol.event.entity.TxGroup;
import io.nuls.protocol.model.*;

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
            String hashHex = hash.getDigestHex();
            Transaction tx = txGroup.getTx(hashHex);
            if (null == tx) {
                tx = temporaryCacheManager.getTx(hashHex);
            }
            if (null == tx) {
                Log.error("get tx not found : " + hashHex + ", from : " + fromId);
                BlockLog.debug("get tx not found : " + hashHex + ", from : " + fromId);
               return;
            }
            txMap.put(tx.getHash().getDigestHex(), tx);
        }

        Block block = ConsensusTool.assemblyBlock(header, txMap, smallBlock.getTxHashList());

        BlockLog.debug("get tx complete of block : " + block.getHeader().getHeight() + " , " + block.getHeader().getHash() + ", from : " + fromId);
        Log.debug("get tx complete of block : " + block.getHeader().getHeight() + " , " + block.getHeader().getHash() + ", from : " + fromId);

//        boolean needForward = blockManager.addBlock(block, true, fromId);
       consensusService.newBlock(block, networkService.getNode(fromId));

        AssembledBlockNotice notice = new AssembledBlockNotice();
        notice.setEventBody(header);
        eventBroadcaster.publishToLocal(notice);

    }
}
