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
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.message.bus.handler.AbstractMessageHandler;
import io.nuls.message.bus.service.MessageBusService;
import io.nuls.network.model.Node;
import io.nuls.protocol.utils.SmallBlockDuplicateRemoval;
import io.nuls.protocol.message.ForwardSmallBlockMessage;
import io.nuls.protocol.message.GetSmallBlockMessage;

/**
 * @author facjas
 */
public class ForwardSmallBlockHandler extends AbstractMessageHandler<ForwardSmallBlockMessage> {

    private MessageBusService messageBusService = NulsContext.getServiceBean(MessageBusService.class);

    @Override
    public void onMessage(ForwardSmallBlockMessage message, Node fromNode) {
        if (message == null || fromNode == null || !fromNode.isHandShake() || null == message.getMsgBody()) {
            return;
        }
        NulsDigestData hash = message.getMsgBody();
        if (!SmallBlockDuplicateRemoval.needDownloadSmallBlock(hash)) {
            return;
        }
        GetSmallBlockMessage getSmallBlockMessage = new GetSmallBlockMessage();
        getSmallBlockMessage.setMsgBody(hash);
        Result result = messageBusService.sendToNode(getSmallBlockMessage, fromNode, true);

        if (result.isFailed()) {
            Log.error("---send getSmallBlockMessage fail, from：" + fromNode.getIp() + ",block hash: " + hash.getDigestHex());
            SmallBlockDuplicateRemoval.removeForward(hash);
            return;
        } else {
            Log.error("---send getSmallBlockMessage success, from：" + fromNode.getIp() + ",block hash: " + hash.getDigestHex());
        }
    }

}
