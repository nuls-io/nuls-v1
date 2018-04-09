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
import io.nuls.consensus.entity.GetBlockParam;
import io.nuls.consensus.entity.NotFound;
import io.nuls.consensus.event.BlockEvent;
import io.nuls.consensus.event.GetBlockRequest;
import io.nuls.consensus.event.NotFoundEvent;
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
    private static final int MAX_SIZE = 10000;
    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);
    private EventBroadcaster eventBroadcaster = NulsContext.getServiceBean(EventBroadcaster.class);

    @Override
    public void onEvent(GetBlockRequest event, String fromId) throws NulsException {
        GetBlockParam param = event.getEventBody();
        if (param.getSize() > MAX_SIZE) {
            return;
        }
        if(param.getSize()==1){
           Block block = this.blockService.getBlockFromMyChain(param.getStartHash().getDigestHex());
           if(null==block){
               sendNotFound(param.getStartHash(),fromId);
               return;
           }
           sendBlock(block,fromId);
           return;
        }
        Block chainStartBlock = this.blockService.getBlockFromMyChain(param.getStartHash().getDigestHex());
        if(null==chainStartBlock){
            sendNotFound(param.getStartHash(),fromId);
            return;
        }
        Block chainEndBlock = this.blockService.getBlockFromMyChain(param.getEndHash().getDigestHex());
        if(null==chainEndBlock){
            sendNotFound(param.getEndHash(),fromId);
            return;
        }
        if(chainEndBlock.getHeader().getHeight()<chainStartBlock.getHeader().getHeight()){
            return;
        }
        long end = param.getStart()+param.getSize()-1;
        if(chainStartBlock.getHeader().getHeight()>param.getStart()||chainEndBlock.getHeader().getHeight()<end){
            sendNotFound(param.getStartHash(),fromId);
            return;
        }

        List<Block> blockList = blockService.getBlockList(param.getStart(),end);

        this.sendBlockList(blockList, fromId);
    }

    private void sendNotFound(NulsDigestData hash, String nodeId) {
        NotFoundEvent event = new NotFoundEvent();
        NotFound data = new NotFound(NotFoundType.BLOCK,hash);
        event.setEventBody(data);
        boolean b = eventBroadcaster.sendToNode(event, nodeId);
        if (!b) {
            Log.warn("send not found failed:" + nodeId + ", hash:" + hash.getDigestHex());
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
