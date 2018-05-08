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

import io.nuls.core.tools.log.BlockLog;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.Result;
import io.nuls.message.bus.handler.AbstractMessageHandler;
import io.nuls.message.bus.service.MessageBusService;
import io.nuls.network.entity.Node;
import io.nuls.protocol.message.BlocksHashMessage;
import io.nuls.protocol.message.GetBlocksHashRequest;
import io.nuls.protocol.model.BlockHashResponse;
import io.nuls.protocol.service.BlockService;

/**
 * @author Niels
 * @date 2018/1/16
 */
public class GetBlocksHashHandler extends AbstractMessageHandler<GetBlocksHashRequest> {

    private static final int MAX_SIZE = 10000;

    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);
    private MessageBusService messageBusService = NulsContext.getServiceBean(MessageBusService.class);

    @Override
    public void onMessage(GetBlocksHashRequest event, Node fromNode) {
    }

    private void sendResponse(BlockHashResponse response, Node fromNode) {
        BlocksHashMessage event = new BlocksHashMessage();
        event.setMsgBody(response);
        Result result = messageBusService.sendToNode(event, fromNode, true);
        if (result.isFailed()) {
            BlockLog.debug("send block hashes to " + fromNode.getId() + " failed!");
        }
    }
}
