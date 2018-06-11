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

package io.nuls.message.bus.handler;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.message.bus.message.GetMessageBodyMessage;
import io.nuls.message.bus.service.MessageBusService;
import io.nuls.message.bus.service.impl.MessageCacheService;
import io.nuls.network.model.Node;
import io.nuls.protocol.message.base.BaseMessage;

/**
 * 获取消息体的处理器
 * The cmd used to get the message body.
 *
 * @author: Charlie
 * @date: 2018/5/8
 */
public class GetMessageBodyHandler extends AbstractMessageHandler<GetMessageBodyMessage> {

    private MessageCacheService messageCacheService = MessageCacheService.getInstance();
    private MessageBusService messageBusService = NulsContext.getServiceBean(MessageBusService.class);
    @Override
    public void onMessage(GetMessageBodyMessage message, Node fromNode) throws NulsException {
        BaseMessage baseMessage = messageCacheService.getSendMessage(message.getMsgBody());
        if (null == baseMessage) {
            Log.warn("get message faild, node:" + fromNode.getId() + ",event:" + message.getMsgBody());
            return;
        }
        messageBusService.sendToNode(baseMessage,fromNode,false);
    }
}
