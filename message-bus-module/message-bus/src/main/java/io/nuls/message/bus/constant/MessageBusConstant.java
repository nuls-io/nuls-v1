/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.message.bus.constant;

import io.nuls.kernel.constant.NulsConstant;

/**
 * 消息总线相关的常量和一些通用的常量定义在这里
 * The relevant constants of the message-bus and some general constants are defined here.
 * @author: Charlie
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
    int DEFAULT_RING_BUFFER_SIZE = 1 << 15;

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
