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
package io.nuls.event.bus.utils.disruptor;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.module.service.ModuleService;
import io.nuls.core.thread.manager.NulsThreadFactory;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.event.bus.module.impl.EventBusModuleBootstrap;
import io.nuls.event.bus.processor.manager.ProcessData;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/10/10
 */
public class DisruptorUtil<T extends DisruptorEvent> {
    private static final DisruptorUtil INSTANCE = new DisruptorUtil();
    private static final Map<String, Disruptor<DisruptorEvent>> DISRUPTOR_MAP = new HashMap<>();

    public static DisruptorUtil getInstance() {
        return INSTANCE;
    }

    private DisruptorUtil() {
    }

    private static final EventFactory EVENT_FACTORY = new EventFactory() {
        @Override
        public Object newInstance() {
            return new DisruptorEvent();
        }
    };

    /**
     * create a disruptor
     *
     * @param name           The title of the disruptor
     * @param ringBufferSize The size of ringBuffer
     */
    public void createDisruptor(String name, int ringBufferSize) {
        if (DISRUPTOR_MAP.keySet().contains(name)) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "create disruptor faild,the name is repetitive!");
        }

        Disruptor<DisruptorEvent> disruptor = new Disruptor<DisruptorEvent>(EVENT_FACTORY,
                ringBufferSize, new NulsThreadFactory(ModuleService.getInstance().getModuleId(EventBusModuleBootstrap.class),name), ProducerType.SINGLE,
                new SleepingWaitStrategy());
        disruptor.handleEventsWith(new EventHandler<DisruptorEvent>() {
            @Override
            public void onEvent(DisruptorEvent disruptorEvent, long l, boolean b) throws Exception {
                Log.debug(disruptorEvent.getData() + "");
            }
        });
        DISRUPTOR_MAP.put(name, disruptor);
    }

    /**
     * start a disruptor service
     *
     * @param name
     */
    public void start(String name) {
        Disruptor<DisruptorEvent> disruptor = DISRUPTOR_MAP.get(name);
        AssertUtil.canNotEmpty(disruptor, "the disruptor is not exist!name:" + name);
        disruptor.start();
    }

    /**
     * start a disruptor service
     *
     * @param name
     */
    public void shutdown(String name) {
        Disruptor<DisruptorEvent> disruptor = DISRUPTOR_MAP.get(name);
        AssertUtil.canNotEmpty(disruptor, "the disruptor is not exist!name:" + name);
        disruptor.shutdown();
    }


    /**
     * add the data obj to the disruptor named the field name
     *
     * @param name
     * @param obj
     */
    public void offer(String name, Object obj) {
        Disruptor<DisruptorEvent> disruptor = DISRUPTOR_MAP.get(name);
        AssertUtil.canNotEmpty(disruptor, "the disruptor is not exist!name:" + name);
        RingBuffer<DisruptorEvent> ringBuffer = disruptor.getRingBuffer();
        //请求下一个事件序号；
        long sequence = ringBuffer.next();
        try {
            //获取该序号对应的事件对象；
            DisruptorEvent event = ringBuffer.get(sequence);
            event.setData(obj);
        } finally {
            //发布事件；
            ringBuffer.publish(sequence);
        }
    }

    /**
     * add some handler to worker pool of the disruptor
     *
     * @param name
     * @param handler
     */
    public void handleEventsWithWorkerPool(String name, WorkHandler<DisruptorEvent<ProcessData>>... handler) {
        Disruptor disruptor = DISRUPTOR_MAP.get(name);
        AssertUtil.canNotEmpty(disruptor, "the disruptor is not exist!name:" + name);
        disruptor.handleEventsWithWorkerPool(handler);


    }

}
