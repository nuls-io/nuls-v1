package io.nuls.event.bus.processor.manager;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.event.EventManager;
import io.nuls.core.event.NulsEvent;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.event.bus.constant.EventBusConstant;
import io.nuls.event.bus.event.handler.NulsEventHandler;
import io.nuls.event.bus.processor.thread.EventBusDispatchThread;
import io.nuls.event.bus.processor.thread.NulsEventCall;
import io.nuls.event.bus.utils.disruptor.DisruptorEvent;
import io.nuls.event.bus.utils.disruptor.DisruptorUtil;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Niels on 2017/11/6.
 * nuls.io
 */
public abstract class ProcessorManager {
    private static final Map<String, NulsEventHandler> handlerMap = new HashMap<>();
    private static final Map<Class, Set<String>> eventHandlerMapping = new HashMap<>();
    private static DisruptorUtil<DisruptorEvent<NulsEvent>> disruptorService = DisruptorUtil.getInstance();
    private static ExecutorService pool ;

    private ProcessorManager() {
    }

    public static final void init() {
        pool =  Executors.newFixedThreadPool(4*EventBusConstant.THREAD_COUNT);
        disruptorService.createDisruptor(EventBusConstant.DISRUPTOR_NAME, EventBusConstant.DEFAULT_RING_BUFFER_SIZE);
        List<EventBusDispatchThread> handlerList = new ArrayList<>();
        for (int i = 0; i < EventBusConstant.THREAD_COUNT; i++) {
            EventBusDispatchThread handler = new EventBusDispatchThread(EventBusConstant.HANDLER_THREAD_NAME + i);
            handlerList.add(handler);
        }
        disruptorService.handleEventsWithWorkerPool(EventBusConstant.DISRUPTOR_NAME, handlerList.toArray(new EventBusDispatchThread[handlerList.size()]));
        disruptorService.start(EventBusConstant.DISRUPTOR_NAME);
    }


    public static void shutdown() {
        disruptorService.shutdown(EventBusConstant.DISRUPTOR_NAME);
    }

    public static void offer(NulsEvent event) {
        EventManager.isLegal(event.getClass());
        disruptorService.offer(EventBusConstant.DISRUPTOR_NAME, event);
    }

    public static String registerEventHandler(Class<? extends NulsEvent> eventClass, NulsEventHandler<? extends NulsEvent> handler) {
        EventManager.isLegal(eventClass);
        AssertUtil.canNotEmpty(eventClass, "registerEventHandler faild");
        AssertUtil.canNotEmpty(handler, "registerEventHandler faild");
        String handlerId = StringUtils.getNewUUID();
        handlerMap.put(handlerId, handler);
        cacheHandlerMapping(eventClass, handlerId);
        return handlerId;
    }

    private static void cacheHandlerMapping(Class<? extends NulsEvent> eventClass, String handlerId) {
        if (eventClass.equals(NulsEvent.class)) {
            return;
        }
        Set<String> ids = eventHandlerMapping.get(eventClass);
        if (null == ids) {
            ids = new HashSet<>();
        }
//        boolean b =
        ids.add(handlerId);
        eventHandlerMapping.put(eventClass,ids);
//        if (!b) {
//            throw new NulsRuntimeException(ErrorCode.FAILED, "registerEventHandler faild");
//        }
        cacheHandlerMapping((Class<NulsEvent>) eventClass.getSuperclass(), handlerId);
    }

    public static void removeEventHandler(String handlerId) {
        handlerMap.remove(handlerId);
    }

    private static Set<NulsEventHandler> getHandlerList(Class<? extends NulsEvent> clazz) {
        if(clazz.equals(NulsEvent.class)){
            return null;
        }
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
        while(!clazz.getSuperclass().equals(NulsEvent.class)){
            set.addAll(getHandlerList((Class<? extends NulsEvent>) clazz.getSuperclass()));
        }
        return set;
    }


    public static void executeHandlers(NulsEvent data) throws InterruptedException {
        if(null==data){
            throw new NulsRuntimeException(ErrorCode.FAILED,"execute event handler faild,the event is null!");
        }
        Set<NulsEventHandler> handlerSet = ProcessorManager.getHandlerList(data.getClass());
        List<NulsEventCall<NulsEvent>> callList = new ArrayList<>();
        for (NulsEventHandler handler : handlerSet) {
            callList.add(new NulsEventCall(data, handler));
        }
        pool.invokeAll(callList);
    }
}
