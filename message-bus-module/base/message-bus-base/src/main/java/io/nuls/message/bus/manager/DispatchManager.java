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

package io.nuls.message.bus.manager;

import io.nuls.core.tools.disruptor.DisruptorData;
import io.nuls.core.tools.disruptor.DisruptorUtil;
import io.nuls.kernel.module.service.ModuleService;
import io.nuls.kernel.thread.manager.NulsThreadFactory;
import io.nuls.message.bus.constant.MessageBusConstant;
import io.nuls.message.bus.module.MessageBusModuleBootstrap;
import io.nuls.message.bus.processor.MessageClassificationProcessor;
import io.nuls.message.bus.model.ProcessData;
import io.nuls.protocol.message.base.BaseMessage;

/**
 * 消息处理的管理器
 * Message processing manager.
 *
 * @author: Charlie
 * @date: 2018/5/6
 */
public class DispatchManager<M extends BaseMessage> {

    private static final DispatchManager INSTANCE = new DispatchManager();

    private DisruptorUtil<DisruptorData<ProcessData<M>>> disruptorService = DisruptorUtil.getInstance();
    private String disruptorName = MessageBusConstant.DISRUPTOR_NAME;
    private MessageClassificationProcessor messageProcesser;

    public static DispatchManager getInstance() {
        return INSTANCE;
    }

    private DispatchManager() {
    }

    public final void init(boolean messageChecking) {
        NulsThreadFactory nulsThreadFactory = new NulsThreadFactory(ModuleService.getInstance().getModuleId(MessageBusModuleBootstrap.class), disruptorName);
        disruptorService.createDisruptor(disruptorName, MessageBusConstant.DEFAULT_RING_BUFFER_SIZE, nulsThreadFactory);

        messageProcesser = new MessageClassificationProcessor();
        disruptorService.handleEventWith(disruptorName, messageProcesser);

        disruptorService.start(disruptorName);
    }

    public void shutdown() {
        messageProcesser.shutdown();
        disruptorService.shutdown(disruptorName);
    }

    public void offer(ProcessData<M> data) {
        disruptorService.offer(disruptorName, data);
    }
}
