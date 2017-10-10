package io.nuls.queue.service;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.nuls.exception.NulsRuntimeException;
import io.nuls.global.NulsContext;
import io.nuls.mq.intf.DisruptorEvent;
import io.nuls.mq.intf.IDisruptorService;
import io.nuls.util.constant.ErrorCode;
import io.nuls.util.param.AssertUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Created by Niels on 2017/10/10.
 * nuls.io
 */
public class DisruptorServiceImpl implements IDisruptorService {
    private static final IDisruptorService service = new DisruptorServiceImpl();
    private static final Map<String, Disruptor<DisruptorEvent>> disruptorMap = new HashMap<>();

    public static IDisruptorService getInstance() {
        return service;
    }

    private DisruptorServiceImpl() {
        NulsContext.getInstance().regService(this);
    }

    private static final EventFactory factory = new EventFactory() {
        @Override
        public Object newInstance() {
            return new DisruptorEvent();
        }
    };

    @Override
    public void createDisruptor(String name, int ringBufferSize) {
        if (disruptorMap.keySet().contains(name)) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "create disruptor faild,the name is repetitive!");
        }

        Disruptor<DisruptorEvent> disruptor = new Disruptor<DisruptorEvent>(factory,
                ringBufferSize, Executors.defaultThreadFactory(), ProducerType.SINGLE,
                new YieldingWaitStrategy());
        disruptorMap.put(name, disruptor);
    }

    @Override
    public void start(String name) {
        Disruptor<DisruptorEvent> disruptor = disruptorMap.get(name);
        AssertUtil.canNotEmpty(disruptor, "the disruptor is not exist!name:" + name);
        disruptor.start();
    }

    @Override
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

    @Override
    public void handleEventsWith(String name, EventHandler<DisruptorEvent> handler) {
        Disruptor disruptor = disruptorMap.get(name);
        AssertUtil.canNotEmpty(disruptor, "the disruptor is not exist!name:" + name);
        disruptor.handleEventsWith(handler);
    }
}
