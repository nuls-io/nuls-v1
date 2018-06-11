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

package io.nuls.message.bus.module;

import io.nuls.kernel.context.NulsContext;
import io.nuls.message.bus.handler.CommonDigestHandler;
import io.nuls.message.bus.handler.GetMessageBodyHandler;
import io.nuls.message.bus.message.CommonDigestMessage;
import io.nuls.message.bus.message.GetMessageBodyMessage;
import io.nuls.message.bus.manager.DispatchManager;
import io.nuls.message.bus.service.MessageBusService;
import io.nuls.message.bus.service.impl.MessageCacheService;

/**
 * @author: Charlie
 * @date: 2018/5/6
 */
public class MessageBusModuleBootstrap extends AbstractMessageBusModule {

    @Override
    public void init() {

    }

    @Override
    public void start() {
        DispatchManager.getInstance().init(true);
        MessageBusService messageBusService = NulsContext.getServiceBean(MessageBusService.class);
        messageBusService.subscribeMessage(CommonDigestMessage.class, new CommonDigestHandler());
        messageBusService.subscribeMessage(GetMessageBodyMessage.class, new GetMessageBodyHandler());
    }

    @Override
    public void shutdown() {
        DispatchManager.getInstance().shutdown();
    }

    @Override
    public void destroy() {
        MessageCacheService.getInstance().destroy();
    }

    @Override
    public String getInfo() {
        return null;
    }
}
