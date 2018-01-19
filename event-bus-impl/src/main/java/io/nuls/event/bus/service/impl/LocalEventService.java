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
package io.nuls.event.bus.service.impl;

import io.nuls.core.event.BaseEvent;
import io.nuls.event.bus.constant.EventBusConstant;
import io.nuls.event.bus.handler.AbstractEventHandler;
import io.nuls.event.bus.processor.manager.ProcessData;
import io.nuls.event.bus.processor.manager.ProcessorManager;

/**
 * @author Niels
 * @date 2017/11/3
 */
public class LocalEventService {

    private static final LocalEventService INSTANCE = new LocalEventService();
    private final ProcessorManager processorManager;

    private LocalEventService() {
        this.processorManager = new ProcessorManager(EventBusConstant.DISRUPTOR_NAME_LOCAL);
    }

    public static LocalEventService getInstance() {
        return INSTANCE;
    }

    public void publish(BaseEvent event) {
        processorManager.offer(new ProcessData(event));
    }

    public String registerEventHandler(Class<? extends BaseEvent> eventClass, AbstractEventHandler<? extends BaseEvent> handler) {
        return this.registerEventHandler(null, eventClass, handler);
    }

    public String registerEventHandler(String handlerId, Class<? extends BaseEvent> eventClass, AbstractEventHandler<? extends BaseEvent> handler) {
        return processorManager.registerEventHandler(handlerId, eventClass, handler);
    }

    public void removeEventHandler(String handlerId) {
        processorManager.removeEventHandler(handlerId);
    }

    public void shutdown() {
        this.processorManager.shutdown();
    }
}
