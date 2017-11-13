package io.nuls.event.bus.constant;

/**
 * Created by Niels on 2017/11/6.
 * nuls.io
 */
public interface EventBusConstant {
    //todo version
    int EVENT_BUS_MODULE_VERSION = 1111;
    //Minimum version supported
    int MINIMUM_VERSION_SUPPORTED = 0;


    String DISRUPTOR_NAME = "nuls-processing-center";
    int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
    int DEFAULT_RING_BUFFER_SIZE = 1 << 20;
    String HANDLER_THREAD_NAME = "processor_handler";
}
