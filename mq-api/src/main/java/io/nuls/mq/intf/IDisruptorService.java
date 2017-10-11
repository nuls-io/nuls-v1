package io.nuls.mq.intf;

import com.lmax.disruptor.EventHandler;

/**
 * Created by Niels on 2017/10/10.
 * nuls.io
 */
public interface IDisruptorService {

    void createDisruptor(String name, int ringBufferSize);

    void start(String name);

    void offer(String name, Object obj);

    void handleEventsWith(String name, EventHandler<DisruptorEvent> handler);

    String getStaticInfo(String name);
}
