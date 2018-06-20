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

package io.nuls.message.bus.processor;

import com.lmax.disruptor.EventHandler;
import io.nuls.core.tools.disruptor.DisruptorData;
import io.nuls.core.tools.log.Log;
import io.nuls.message.bus.constant.MessageBusConstant;
import io.nuls.message.bus.message.CommonDigestMessage;
import io.nuls.message.bus.model.ProcessData;
import io.nuls.message.bus.service.impl.MessageCacheService;
import io.nuls.protocol.constant.ProtocolConstant;
import io.nuls.protocol.message.base.BaseMessage;

/**
 * @author: Charlie
 * @date: 2018/5/6
 */
public class MessageFilterProcessor<E extends BaseMessage> implements EventHandler<DisruptorData<ProcessData<E>>> {

    private MessageCacheService messageCacheService = MessageCacheService.getInstance();

    @Override
    public void onEvent(DisruptorData<ProcessData<E>> processDataDisruptorMessage, long l, boolean b) {
        try {
            BaseMessage message = processDataDisruptorMessage.getData().getData();
            if (null == message || message.getHeader() == null) {
                return;
            }

            boolean commonDigestTx = message.getHeader().getMsgType() == MessageBusConstant.MSG_TYPE_COMMON_MSG_HASH_MSG &&
                    message.getHeader().getModuleId() == MessageBusConstant.MODULE_ID_MESSAGE_BUS;

            if (!commonDigestTx) {
                boolean needCache = message.getHeader().getMsgType() == ProtocolConstant.PROTOCOL_NEW_BLOCK &&
                        message.getHeader().getModuleId() == ProtocolConstant.MODULE_ID_PROTOCOL;
                if(!needCache) {
                    needCache = message.getHeader().getMsgType() == ProtocolConstant.PROTOCOL_NEW_TX &&
                            message.getHeader().getModuleId() == ProtocolConstant.MODULE_ID_PROTOCOL;
                }
                if(needCache) {
                    messageCacheService.cacheRecievedMessageHash(message.getHash());
                }
                return;
            }
            if (messageCacheService.kownTheMessage(message.getHash())) {
                processDataDisruptorMessage.setStoped(true);
            }else if (messageCacheService.kownTheMessage(((CommonDigestMessage) message).getMsgBody())) {
                processDataDisruptorMessage.setStoped(true);
            } else {
                messageCacheService.cacheRecievedMessageHash(message.getHash());
            }
        } catch (Exception e) {
            Log.error(e);
        }
    }
}
