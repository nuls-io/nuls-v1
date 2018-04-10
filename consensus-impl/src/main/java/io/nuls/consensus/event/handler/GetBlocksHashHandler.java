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

import io.nuls.consensus.constant.NotFoundType;
import io.nuls.consensus.entity.BlockHashResponse;
import io.nuls.consensus.entity.NotFound;
import io.nuls.consensus.event.BlocksHashEvent;
import io.nuls.consensus.event.GetBlocksHashRequest;
import io.nuls.consensus.event.NotFoundEvent;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.log.Log;
import io.nuls.db.entity.BlockHeaderPo;
import io.nuls.event.bus.handler.AbstractEventHandler;
import io.nuls.event.bus.service.intf.EventBroadcaster;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 * @date 2018/1/16
 */
public class GetBlocksHashHandler extends AbstractEventHandler<GetBlocksHashRequest> {

    private static final int MAX_SIZE = 10000;

    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);
    private EventBroadcaster eventBroadcaster = NulsContext.getServiceBean(EventBroadcaster.class);

    @Override
    public void onEvent(GetBlocksHashRequest event, String fromId) {
        if (MAX_SIZE < event.getEventBody().getSize()) {
            return;
        }
        long end = event.getEventBody().getStart() + event.getEventBody().getSize() - 1;
        if (end > NulsContext.getInstance().getBestBlock().getHeader().getHeight()) {
            NotFoundEvent notFoundEvent  = new NotFoundEvent();
            notFoundEvent.setEventBody(new NotFound(NotFoundType.HASHES,event.getHash()));
            this.eventBroadcaster.sendToNode(notFoundEvent,fromId);
            return;
        }
        BlockHashResponse response = new BlockHashResponse();
        response.setRequestEventHash(event.getHash());
        if (1L == event.getEventBody().getSize() && event.getEventBody().getStart() <= 0L) {
            Block block = NulsContext.getInstance().getBestBlock();
            response.put(block.getHeader().getHeight(), block.getHeader().getHash());
        } else {
            List<BlockHeaderPo> list = this.blockService.getBlockHashList(event.getEventBody().getStart(), end);
            long lastHeight = 0L;
            for (int i = 0; i < list.size(); i++) {
                BlockHeaderPo po = list.get(i);
                response.put(po.getHeight(), NulsDigestData.fromDigestHex(po.getHash()));
                lastHeight = po.getHeight();
            }
            if (lastHeight < end) {
                for (long height = lastHeight + 1; height <= end; height++) {
                    Block block = this.blockService.getBlock(height);
                    response.put(block.getHeader().getHeight(), block.getHeader().getHash());
                }
            }
        }
        sendResponse(response, fromId);
    }

    private void sendResponse(BlockHashResponse response, String fromId) {
        BlocksHashEvent event = new BlocksHashEvent();
        event.setEventBody(response);
        boolean result = eventBroadcaster.sendToNode(event, fromId);
    }
}
