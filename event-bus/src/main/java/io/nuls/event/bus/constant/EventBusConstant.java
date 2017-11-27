package io.nuls.event.bus.constant;

/**
 * Created by Niels on 2017/11/6.
 *
 */
public interface EventBusConstant {
    //version
    int EVENT_BUS_MODULE_VERSION = 1111;
    //Minimum version supported
    int MINIMUM_VERSION_SUPPORTED = 0;


    String DISRUPTOR_NAME_LOCAL = "nuls-processing-local";
    String DISRUPTOR_NAME_NETWORK = "nuls-processing-network";
    int THREAD_COUNT = 2*Runtime.getRuntime().availableProcessors();
    String THREAD_POOL_NAME = "nuls-processor-dispatcher";
    int DEFAULT_RING_BUFFER_SIZE = 1 << 20;


}
