package io.nuls.event.bus.utils.disruptor;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.LiteBlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.event.BaseNulsEvent;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.param.AssertUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 *
 * @author Niels
 * @date 2017/10/10
 *
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
                ringBufferSize, Executors.defaultThreadFactory(), ProducerType.SINGLE,
                new LiteBlockingWaitStrategy());
//        disruptor.handleEventsWith(new EventHandler<DisruptorEvent>() {
//            @Override
//            public void onEvent(DisruptorEvent disruptorEvent, long l, boolean b) throws Exception {
//                Log.debug(disruptorEvent.getData() + "");
//            }
//        });
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
    public void handleEventsWithWorkerPool(String name, WorkHandler<DisruptorEvent<BaseNulsEvent>>... handler) {
        Disruptor disruptor = DISRUPTOR_MAP.get(name);
        AssertUtil.canNotEmpty(disruptor, "the disruptor is not exist!name:" + name);
        disruptor.handleEventsWithWorkerPool(handler);


    }

}
