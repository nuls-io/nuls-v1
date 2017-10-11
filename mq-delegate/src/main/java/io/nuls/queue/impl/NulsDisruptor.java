package io.nuls.queue.impl;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.nuls.mq.intf.StatInfo;

import java.util.concurrent.ThreadFactory;

/**
 * Created by Niels on 2017/10/11.
 * nuls.io
 */
public class NulsDisruptor<T> extends Disruptor<T> {


    public NulsDisruptor(EventFactory<T> eventFactory, int ringBufferSize, ThreadFactory threadFactory) {
        super(eventFactory, ringBufferSize, threadFactory);
    }

    public NulsDisruptor(EventFactory<T> eventFactory, int ringBufferSize, ThreadFactory threadFactory, ProducerType producerType, WaitStrategy waitStrategy) {
        super(eventFactory, ringBufferSize, threadFactory, producerType, waitStrategy);
    }
    private StatInfo statInfo;

    private String name;

    public StatInfo getStatInfo() {
        return statInfo;
    }

    public void setStatInfo(StatInfo statInfo) {
        this.statInfo = statInfo;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
