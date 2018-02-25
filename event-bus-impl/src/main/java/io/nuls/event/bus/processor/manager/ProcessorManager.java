/**
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
 */
package io.nuls.event.bus.processor.manager;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.event.BaseEvent;
import io.nuls.core.event.EventManager;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.module.service.ModuleService;
import io.nuls.core.thread.manager.NulsThreadFactory;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.event.bus.constant.EventBusConstant;
import io.nuls.event.bus.handler.intf.NulsEventHandler;
import io.nuls.event.bus.module.intf.AbstractEventBusModule;
import io.nuls.event.bus.processor.thread.EventDispatchThread;
import io.nuls.event.bus.processor.thread.NulsEventCall;
import io.nuls.event.bus.utils.disruptor.DisruptorEvent;
import io.nuls.event.bus.utils.disruptor.DisruptorUtil;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author Niels
 * @date 2017/11/6
 */
public class ProcessorManager<E extends io.nuls.core.event.BaseEvent, H extends NulsEventHandler<? extends BaseEvent>> {
    private final Map<String, H> handlerMap = new HashMap<>();
    private final Map<Class, Set<String>> eventHandlerMapping = new HashMap<>();
    private DisruptorUtil<DisruptorEvent<ProcessData<E>>> disruptorService = DisruptorUtil.getInstance();
    private ExecutorService pool;
    private String disruptorName;

    public ProcessorManager(String disruptorName) {
        this.disruptorName = disruptorName;
        this.init();
    }

    public final void init() {

        pool = TaskManager.createThreadPool(EventBusConstant.THREAD_COUNT,0,
                new NulsThreadFactory(NulsConstant.MODULE_ID_EVENT_BUS, EventBusConstant.THREAD_POOL_NAME));
        disruptorService.createDisruptor(disruptorName, EventBusConstant.DEFAULT_RING_BUFFER_SIZE);
        List<EventDispatchThread> handlerList = new ArrayList<>();
        for (int i = 0; i < EventBusConstant.THREAD_COUNT; i++) {
            EventDispatchThread handler = new EventDispatchThread(this);
            handlerList.add(handler);
        }
        disruptorService.handleEventsWithWorkerPool(disruptorName, handlerList.toArray(new EventDispatchThread[handlerList.size()]));
        disruptorService.start(disruptorName);
    }


    public void shutdown() {
        disruptorService.shutdown(disruptorName);
    }

    public void offer(ProcessData<E> data) {
        EventManager.care(data.getData().getClass());
        disruptorService.offer(disruptorName, data);
    }

    public String registerEventHandler(String handlerId, Class<E> eventClass, H handler) {
        EventManager.putEvent(eventClass);
        AssertUtil.canNotEmpty(eventClass, "registerEventHandler faild");
        AssertUtil.canNotEmpty(handler, "registerEventHandler faild");
        if (StringUtils.isBlank(handlerId)) {
            handlerId = StringUtils.getNewUUID();
        }
        handlerMap.put(handlerId, handler);
        cacheHandlerMapping(eventClass, handlerId);
        return handlerId;
    }

    private void cacheHandlerMapping(Class<E> eventClass, String handlerId) {

        Set<String> ids = eventHandlerMapping.get(eventClass);
        if (null == ids) {
            ids = new HashSet<>();
        }
//        boolean b =
        ids.add(handlerId);
        eventHandlerMapping.put(eventClass, ids);
//        if (!b) {
//            throw new NulsRuntimeException(ErrorCode.FAILED, "registerEventHandler faild");
//        }
//        cacheHandlerMapping((Class<E>) eventClass.getSuperclass(), handlerId);
    }

    public void removeEventHandler(String handlerId) {
        handlerMap.remove(handlerId);
    }

    private Set<NulsEventHandler> getHandlerList(Class<E> clazz) {
        Set<String> ids = eventHandlerMapping.get(clazz);
        Set<NulsEventHandler> set = new HashSet<>();
        do {
            if (null == ids || ids.isEmpty()) {
                break;
            }
            for (String id : ids) {
                if (StringUtils.isBlank(id)) {
                    continue;
                }
                NulsEventHandler handler = handlerMap.get(id);
                if (null == handler) {
                    continue;
                }
                set.add(handler);
            }
        } while (false);
        if (!clazz .equals(BaseEvent.class)) {
            set.addAll(getHandlerList((Class<E>) clazz.getSuperclass()));
        }
        return set;
    }


    public void executeHandlers(ProcessData<E> data) throws InterruptedException {
        if (null == data) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "execute event handler faild,the event is null!");
        }
        Set<NulsEventHandler> handlerSet = this.getHandlerList((Class<E>) data.getData().getClass());
        for (NulsEventHandler handler : handlerSet) {
            pool.execute(new NulsEventCall(data, handler));
        }
    }
}
