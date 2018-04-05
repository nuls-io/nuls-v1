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

import io.nuls.consensus.event.BlockEvent;
import io.nuls.consensus.event.GetBlockRequest;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.handler.AbstractEventHandler;
import io.nuls.event.bus.service.intf.EventBroadcaster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author facjas
 * @date 2017/11/16
 */
public class GetBlockHandler extends AbstractEventHandler<GetBlockRequest> {

    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);
    private EventBroadcaster eventBroadcaster = NulsContext.getServiceBean(EventBroadcaster.class);

    @Override
    public void onEvent(GetBlockRequest event, String fromId) throws NulsException {
        if (event.getToHash() != null) {
            askByHash(event, fromId);
            return;
        }
        if (event.getEnd() > NulsContext.getInstance().getBestHeight()) {
            return;
        }
        List<Block> blockList = blockService.getBlockList(event.getStart(), event.getEnd());
        if (blockList.size() != (event.getEnd() - event.getStart() + 1)) {
            Log.warn("count wrong!");
        }
        this.sendBlockList(blockList, fromId);
    }

    private void askByHash(GetBlockRequest event, String fromId) throws NulsException {
        Block block = blockService.getBlock(event.getToHash().getDigestHex());
        if (null == block) {
            Log.debug("I don't have the block:" + event.getToHash());
            return;
        }
        if (event.getPreHash() == null || block.getHeader().getPreHash().equals(event.getPreHash())) {
            sendBlock(block, fromId);
            return;
        }
        Block preBlock = blockService.getBlock(event.getPreHash().getDigestHex());
        if (null == preBlock) {
            Log.debug("I don't have the block:" + event.getPreHash());
            return;
        }
        List<Block> blockList = blockService.getBlockList(preBlock.getHeader().getHeight() + 1, block.getHeader().getHeight());
        if (!blockList.isEmpty() && blockList.get(blockList.size() - 1).getHeader().getHash().equals(event.getToHash())) {
            sendBlockList(blockList, fromId);
            return;
        }
        List<Block> resultBlockList = new ArrayList<>();
        Map<NulsDigestData, Block> blockMap = new HashMap<>();
        for (Block b : blockList) {
            blockMap.put(b.getHeader().getHash(), b);
        }
        resultBlockList.add(0, block);
        boolean success = true;
        String nextHash = block.getHeader().getPreHash().getDigestHex();
        while (true) {
            Block b = blockMap.get(nextHash);
            if (b == null) {
                b = blockService.getBlock(nextHash);
            }
            if (b == null) {
                success = false;
                break;
            }
            if (b.getHeader().getHash().equals(event.getPreHash())) {
                break;
            }
            resultBlockList.add(0, b);
            nextHash = b.getHeader().getPreHash().getDigestHex();
        }
        if (success) {
            this.sendBlockList(resultBlockList, fromId);
        }
    }

    private void sendBlockList(List<Block> blockList, String nodeId) {
        for (Block block : blockList) {
            this.sendBlock(block, nodeId);
        }
    }

    private void sendBlock(Block block, String nodeId) {
        if (null == block) {
            Log.warn("there is a null block");
            return;
        }
        BlockEvent blockEvent = new BlockEvent();
        blockEvent.setEventBody(block);
        boolean b = eventBroadcaster.sendToNode(blockEvent, nodeId);
        if (!b) {
            Log.warn("send block failed:" + nodeId + ",height:" + block.getHeader().getHeight());
        }
    }

}
