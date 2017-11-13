package io.nuls.event.bus.utils.disruptor;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.event.NulsEvent;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.param.AssertUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Created by Niels on 2017/10/10.
 * nuls.io
 */
public class DisruptorUtil<T extends DisruptorEvent> {
    private static final DisruptorUtil service = new DisruptorUtil();
    private static final Map<String, Disruptor<DisruptorEvent>> disruptorMap = new HashMap<>();

    public static DisruptorUtil getInstance() {
        return service;
    }

    private DisruptorUtil() {
    }

    private static final EventFactory factory = new EventFactory() {
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
        if (disruptorMap.keySet().contains(name)) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "create disruptor faild,the name is repetitive!");
        }

        Disruptor<DisruptorEvent> disruptor = new Disruptor<DisruptorEvent>(factory,
                ringBufferSize, Executors.defaultThreadFactory(), ProducerType.SINGLE,
                new YieldingWaitStrategy());
//        disruptor.handleEventsWith(new EventHandler<DisruptorEvent>() {
//            @Override
//            public void onEvent(DisruptorEvent disruptorEvent, long l, boolean b) throws Exception {
//                Log.debug(disruptorEvent.getData() + "");
//            }
//        });
        disruptorMap.put(name, disruptor);
    }

    /**
     * start a disruptor service
     *
     * @param name
     */
    public void start(String name) {
        Disruptor<DisruptorEvent> disruptor = disruptorMap.get(name);
        AssertUtil.canNotEmpty(disruptor, "the disruptor is not exist!name:" + name);
        disruptor.start();
    }

    /**
     * start a disruptor service
     *
     * @param name
     */
    public void shutdown(String name) {
        Disruptor<DisruptorEvent> disruptor = disruptorMap.get(name);
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
        Disruptor<DisruptorEvent> disruptor = disruptorMap.get(name);
        AssertUtil.canNotEmpty(disruptor, "the disruptor is not exist!name:" + name);
        RingBuffer<DisruptorEvent> ringBuffer = disruptor.getRingBuffer();
        long sequence = ringBuffer.next();//请求下一个事件序号；
        try {
            DisruptorEvent event = ringBuffer.get(sequence);//获取该序号对应的事件对象；
            event.setData(obj);
        } finally {
            ringBuffer.publish(sequence);//发布事件；
        }
    }

    /**
     * add some handler to worker pool of the disruptor
     *
     * @param name
     * @param handler
     */
    public void handleEventsWithWorkerPool(String name, WorkHandler<DisruptorEvent<NulsEvent>>... handler) {
        Disruptor disruptor = disruptorMap.get(name);
        AssertUtil.canNotEmpty(disruptor, "the disruptor is not exist!name:" + name);
        disruptor.handleEventsWithWorkerPool(handler);


    }

}
