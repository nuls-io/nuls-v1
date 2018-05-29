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

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.message.bus.handler.AbstractMessageHandler;
import io.nuls.message.bus.service.MessageBusService;
import io.nuls.network.model.Node;
import io.nuls.protocol.constant.NotFoundType;
import io.nuls.protocol.message.BlockMessage;
import io.nuls.protocol.message.GetBlockMessage;
import io.nuls.protocol.message.NotFoundMessage;
import io.nuls.protocol.model.NotFound;
import io.nuls.protocol.service.BlockService;

/**
 * @author facjas
 * @date 2017/11/16
 */
public class GetBlockHandler extends AbstractMessageHandler<GetBlockMessage> {

    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);
    private MessageBusService messageBusService = NulsContext.getServiceBean(MessageBusService.class);

    @Override
    public void onMessage(GetBlockMessage message, Node fromNode) {

        if(message == null || message.getMsgBody() == null || fromNode == null) {
            return;
        }

        Block block= null;
        Result<Block> result = blockService.getBlock(message.getBlockHash());
        if (result.isFailed() || (block = result.getData()) == null) {
            sendNotFound(message.getBlockHash(), fromNode);
            return;
        }
        sendBlock(block, fromNode);
    }

    private void sendNotFound(NulsDigestData hash, Node node) {
        NotFoundMessage message = new NotFoundMessage();
        NotFound data = new NotFound(NotFoundType.BLOCK, hash);
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
