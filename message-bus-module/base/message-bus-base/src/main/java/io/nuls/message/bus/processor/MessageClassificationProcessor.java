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
import io.nuls.kernel.thread.manager.NulsThreadFactory;
import io.nuls.kernel.thread.manager.TaskManager;
import io.nuls.message.bus.constant.MessageBusConstant;
import io.nuls.message.bus.handler.intf.NulsMessageHandler;
import io.nuls.message.bus.manager.HandlerManager;
import io.nuls.message.bus.model.ProcessData;
import io.nuls.message.bus.processor.thread.NulsMessageCall;
import io.nuls.protocol.message.base.BaseMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author ln
 * @date 2018-05-23
 */
public class MessageClassificationProcessor<E extends BaseMessage> implements EventHandler<DisruptorData<ProcessData<E>>> {

    private HandlerManager handlerManager = HandlerManager.getInstance();
    private Map<Class<? extends BaseMessage>, ExecutorService> handlerService = new HashMap<>();

    @Override
    public void onEvent(DisruptorData<ProcessData<E>> disruptorData, long l, boolean b) throws Exception {

        if (null == disruptorData || disruptorData.getData() == null) {
            Log.warn("there is null data in disruptorData!");
            return;
        }
        if (disruptorData.isStoped()) {
            disruptorData.setStoped(false);
            return;
        }

        ProcessData processData = disruptorData.getData();
        Class<? extends BaseMessage> serviceId = processData.getData().getClass();
        Set<NulsMessageHandler> handlers = handlerManager.getHandlerList(serviceId);
        ThreadPoolExecutor handlerExecutor = (ThreadPoolExecutor) handlerService.get(serviceId);
        if (handlerExecutor == null) {
            handlerExecutor = TaskManager.createThreadPool(1, Integer.MAX_VALUE, new NulsThreadFactory(MessageBusConstant.MODULE_ID_MESSAGE_BUS, "disruptor-processor"));
            handlerService.put(serviceId, handlerExecutor);
        }
        for (NulsMessageHandler handler : handlers) {
            handlerExecutor.execute(new NulsMessageCall(processData, handler));
            int size = handlerExecutor.getQueue().size();
            if (size > 10) {
                System.out.println(serviceId + " queue size::::::::::::::::::::" + size);
            }
        }
    }

    public void shutdown() {
        if (handlerService == null) {
            return;
        }
        for (Map.Entry<Class<? extends BaseMessage>, ExecutorService> entry : handlerService.entrySet()) {
            entry.getValue().shutdown();
        }
    }
}
