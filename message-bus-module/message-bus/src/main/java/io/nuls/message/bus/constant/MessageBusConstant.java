package io.nuls.message.bus.constant;

import io.nuls.kernel.constant.NulsConstant;

/**
 * 消息总线相关的常量和一些通用的常量定义在这里
 * The relevant constants of the message-bus and some general constants are defined here.
 * @author: Charlie
 * @date: 2018/5/6
 */
public interface MessageBusConstant extends NulsConstant {


    /**
     * The module id of the message-bus module
     */
    short MODULE_ID_MESSAGE_BUS = 6;

    /**
     * The name of the disruptor
     */
    String DISRUPTOR_NAME = "nuls-processing";

    /**
     * 线程池的默认线程数量
     * The default number of threads in the thread pool
     */
    int THREAD_COUNT = 2 * Runtime.getRuntime().availableProcessors();

    /**
     * 线程池的名称
     * The name of the thread pool
     */
    String THREAD_POOL_NAME = "nuls-process-dispatcher";

    /**
     * The default size of ringBuffer
     */
    int DEFAULT_RING_BUFFER_SIZE = 1 << 20;

    /**
     * 消息类型为通用消息hash的消息
     */
    short MSG_TYPE_COMMON_MSG_HASH_MSG = 1;

    /**
     * 消息类型为获取消息体的消息
     * The message type is the message to get the message body
     */
    short MSG_TYPE_GET_MSG_BODY_MSG = 2;

}
