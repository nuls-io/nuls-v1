package io.nuls.mq.service.impl;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.ProducerType;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.mq.entity.DisruptorEvent;
import io.nuls.mq.entity.impl.NulsDisruptor;
import io.nuls.mq.entity.impl.NulsDisruptorHandler;
import io.nuls.mq.entity.impl.StatInfoImpl;
import io.nuls.mq.intf.DisruptorService;
import io.nuls.mq.manager.QueueManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Created by Niels on 2017/10/10.
 * nuls.io
 */
public class DisruptorServiceImpl implements DisruptorService {
    private static final DisruptorService service = new DisruptorServiceImpl();
    private static final Map<String, NulsDisruptor<DisruptorEvent>> disruptorMap = new HashMap<>();

    public static DisruptorService getInstance() {
        return service;
    }

    private DisruptorServiceImpl() {
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

        NulsDisruptor<DisruptorEvent> disruptor = new NulsDisruptor<DisruptorEvent>(factory,
                ringBufferSize, Executors.defaultThreadFactory(), ProducerType.SINGLE,
                new YieldingWaitStrategy());
        disruptor.setName(name);
        disruptor.setStatInfo(new StatInfoImpl(name, 0, QueueManager.getLatelySecond()));
        disruptor.handleEventsWith(new NulsDisruptorHandler<>(null,disruptor.getStatInfo()));
        disruptorMap.put(name, disruptor);
    }

    @Override
    public void start(String name) {
        NulsDisruptor<DisruptorEvent> disruptor = disruptorMap.get(name);
        AssertUtil.canNotEmpty(disruptor, "the disruptor is not exist!name:" + name);
        disruptor.start();
    }

    @Override
    public void offer(String name, Object obj) {
        NulsDisruptor<DisruptorEvent> disruptor = disruptorMap.get(name);
        AssertUtil.canNotEmpty(disruptor, "the disruptor is not exist!name:" + name);
        RingBuffer<DisruptorEvent> ringBuffer = disruptor.getRingBuffer();
        long sequence = ringBuffer.next();//请求下一个事件序号；
        try {
            DisruptorEvent event = ringBuffer.get(sequence);//获取该序号对应的事件对象；
            event.setData(obj);
            disruptor.getStatInfo().putOne();
        } finally {
            ringBuffer.publish(sequence);//发布事件；
        }
    }

    @Override
    public void handleEventsWith(String name, EventHandler<DisruptorEvent> handler) {
        NulsDisruptor disruptor = disruptorMap.get(name);
        AssertUtil.canNotEmpty(disruptor, "the disruptor is not exist!name:" + name);
        disruptor.handleEventsWith(new NulsDisruptorHandler(handler, disruptor.getStatInfo()));
    }
    @Override
    public void handleEventsWithWorkerPool(String name, WorkHandler<DisruptorEvent> ...handler) {
        NulsDisruptor disruptor = disruptorMap.get(name);
        AssertUtil.canNotEmpty(disruptor, "the disruptor is not exist!name:" + name);
        disruptor.handleEventsWithWorkerPool(handler);
    }
    @Override
    public String getStatInfo(String name) {
        NulsDisruptor disruptor = disruptorMap.get(name);
        AssertUtil.canNotEmpty(disruptor, "the disruptor is not exist!name:" + name);
        return disruptor.getStatInfo().toString();
    }
}
