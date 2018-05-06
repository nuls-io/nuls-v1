package io.nuls.message.bus.constant;

/**
 * @author: Charlie
 * @date: 2018/5/6
 */
public interface MessageBusConstant {

    String DISRUPTOR_NAME = "nuls-processing";
    int THREAD_COUNT = 2 * Runtime.getRuntime().availableProcessors();
    String THREAD_POOL_NAME = "nuls-process-dispatcher";
    int DEFAULT_RING_BUFFER_SIZE = 1 << 20;
}
