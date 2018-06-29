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

import io.nuls.kernel.context.NulsContext;
import io.nuls.message.bus.filter.NulsMessageFilter;
import io.nuls.message.bus.filter.NulsMessageFilterChain;
import io.nuls.message.bus.handler.intf.NulsMessageHandler;
import io.nuls.message.bus.service.MessageBusService;
import io.nuls.protocol.message.base.BaseMessage;

/**
 * 消息处理器的实现类(抽象的)
 * Message cmd implementation class (abstract)
 * @author: Charlie
 */
public abstract class AbstractMessageHandler<T extends BaseMessage> implements NulsMessageHandler<T> {

    protected MessageBusService messageBusService = NulsContext.getServiceBean(MessageBusService.class);

    private NulsMessageFilterChain filterChain = new NulsMessageFilterChain();

    @Override
    public void addFilter(NulsMessageFilter<T> filter) {
        filterChain.addFilter(filter);
    }

    @Override
    public NulsMessageFilterChain getFilterChian() {
        return filterChain;
    }
}
