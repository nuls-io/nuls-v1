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

import io.nuls.consensus.entity.BlockHashResponse;
import io.nuls.consensus.event.BlocksHashEvent;
import io.nuls.consensus.event.GetBlocksHashRequest;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.handler.AbstractEventHandler;
import io.nuls.event.bus.service.intf.EventBroadcaster;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 * @date 2018/1/16
 */
public class GetBlocksHashHandler extends AbstractEventHandler<GetBlocksHashRequest> {

    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);
    private EventBroadcaster eventBroadcaster = NulsContext.getServiceBean(EventBroadcaster.class);

    @Override
    public void onEvent(GetBlocksHashRequest event, String fromId) {
        if (event.getEventBody().getEnd() > NulsContext.getInstance().getBestBlock().getHeader().getHeight()) {
            return;
        }
        boolean b = event.getEventBody().getStart() == event.getEventBody().getEnd();
        if (b) {
            BlockHashResponse response = new BlockHashResponse();
            Block block;
            if (event.getEventBody().getEnd() <= 0) {
                block = NulsContext.getInstance().getBestBlock();
            } else {
                block = blockService.getBlock(event.getEventBody().getEnd());
            }
            if (null == block) {
                Log.warn("block can not get:"+event.getEventBody().getEnd());
                return;
            }
            response.put(block.getHeader().getHeight(), block.getHeader().getHash());
            sendResponse(response, fromId);
        } else {
            List<BlockHeader> list = this.blockService.getBlockHeaderList(event.getEventBody().getStart(), event.getEventBody().getEnd(), event.getEventBody().getSplit());
            List<Long> resultHeightList = new ArrayList<>();
            List<NulsDigestData> resultHashList = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                resultHeightList.add(list.get(i).getHeight());
                resultHashList.add(list.get(i).getHash());
            }
            if (resultHeightList.isEmpty() || resultHeightList.get(resultHeightList.size() - 1) < event.getEventBody().getEnd()) {
                Block block = this.blockService.getBlock(event.getEventBody().getEnd());
                if(block==null){

                    //todo why?
                    Log.warn("block can not get:"+event.getEventBody().getEnd());
                    return ;
                }
                resultHeightList.add(block.getHeader().getHeight());
                resultHashList.add(block.getHeader().getHash());
            }
            final int size = 50000;
            for (int i = 0; i < resultHashList.size(); i += size) {
                BlockHashResponse response = new BlockHashResponse();
                int end = i + size;
                if (end > resultHeightList.size()) {
                    end = resultHeightList.size();
                }
                response.setHeightList(resultHeightList.subList(i, end));
                response.setHashList(resultHashList.subList(i, end));
                sendResponse(response, fromId);
            }
        }
    }

    private void sendResponse(BlockHashResponse response, String fromId) {
        BlocksHashEvent event = new BlocksHashEvent();
        event.setEventBody(response);
        boolean result = eventBroadcaster.sendToNode(event, fromId);
    }
}
