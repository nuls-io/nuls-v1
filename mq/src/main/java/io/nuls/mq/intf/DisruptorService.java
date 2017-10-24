package io.nuls.mq.intf;

import com.lmax.disruptor.EventHandler;
import io.nuls.mq.entity.DisruptorEvent;

/**
 * Created by Niels on 2017/10/10.
 * nuls.io
 */
public interface DisruptorService {
    /**
     * create a disruptor
     * @param name The title of the disruptor
     * @param ringBufferSize The size of ringBuffer
     */
    void createDisruptor(String name, int ringBufferSize);

    /**
     * start a disruptor service
     * @param name
     */
    void start(String name);

    /**
     * add the data obj to the disruptor named the field name
     * @param name
     * @param obj
     */
    void offer(String name, Object obj);

    /**
     * add a handler for the disruptor
     * @param name
     * @param handler
     */
    void handleEventsWith(String name, EventHandler<DisruptorEvent> handler);

    /**
     * get the info of running status
     * @param name
     * @return
     */
    String getStatInfo(String name);
}
