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

import io.nuls.core.tools.array.ArraysTool;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.utils.SerializeUtils;
import io.nuls.message.bus.handler.AbstractMessageHandler;
import io.nuls.message.bus.service.MessageBusService;
import io.nuls.network.model.Node;
import io.nuls.protocol.constant.NotFoundType;
import io.nuls.protocol.message.*;
import io.nuls.protocol.model.CompleteParam;
import io.nuls.protocol.model.GetBlocksByHashParam;
import io.nuls.protocol.model.GetBlocksByHeightParam;
import io.nuls.protocol.model.NotFound;
import io.nuls.protocol.service.BlockService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author facjas
 * @date 2017/11/16
 */
public class GetBlocksByHeightHandler extends AbstractMessageHandler<GetBlocksByHeightMessage> {

    private static final int MAX_SIZE = 1000;
    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);
    private MessageBusService messageBusService = NulsContext.getServiceBean(MessageBusService.class);

    @Override
    public void onMessage(GetBlocksByHeightMessage message, Node fromNode) {

        if(message == null || message.getMsgBody() == null || fromNode == null) {
            return;
        }

        GetBlocksByHeightParam param = message.getMsgBody();
        if(param.getStartHeight() < 0L || param.getStartHeight() > param.getEndHeight()) {
            return;
        }

        BlockHeader startBlockHeader = blockService.getBlockHeader(param.getStartHeight()).getData();
        if(startBlockHeader == null) {
            sendNotFound(message.getHash(), fromNode);
            return;
        }
        Block endBlock = blockService.getBlock(param.getEndHeight()).getData();
        if(endBlock == null) {
            sendNotFound(message.getHash(), fromNode);
            return;
        }
        if(endBlock.getHeader().getHeight() - startBlockHeader.getHeight() >= MAX_SIZE) {
            return;
        }

        Block block = endBlock;
        while(true) {
            sendBlock(block, fromNode);
            if(block.getHeader().getHash().equals(startBlockHeader.getHash())) {
                break;
            }
            Result<Block> result = blockService.getBlock(block.getHeader().getPreHash());
            if (result.isFailed() || (block = result.getData()) == null) {
                sendNotFound(message.getHash(), fromNode);
                return;
            }
        }

        CompleteMessage completeMessage = new CompleteMessage();
        completeMessage.setMsgBody(new CompleteParam(message.getHash(), true));
        messageBusService.sendToNode(completeMessage, fromNode, true);
    }

    private void sendNotFound(NulsDigestData hash, Node node) {
        NotFoundMessage message = new NotFoundMessage();
        NotFound data = new NotFound(NotFoundType.BLOCKS, hash);
        message.setMsgBody(data);
        Result result = this.messageBusService.sendToNode(message, node, true);
        if (result.isFailed()) {
            Log.warn("send BLOCK NotFound failed:" + node.getId() + ", hash:" + hash);
        }
    }

    private void sendBlock(Block block, Node fromNode) {
        BlockMessage blockMessage = new BlockMessage();
        blockMessage.setMsgBody(block);
        Result result = this.messageBusService.sendToNode(blockMessage, fromNode, true);
        if (result.isFailed()) {
            Log.warn("send block failed:" + fromNode.getId() + ",height:" + block.getHeader().getHeight());
        }
    }
}
