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

package io.nuls.message.bus.service;

import io.nuls.kernel.model.Result;
import io.nuls.message.bus.handler.intf.NulsMessageHandler;
import io.nuls.network.model.Node;
import io.nuls.protocol.message.base.BaseMessage;

import java.util.List;

/**
 * 消息总线模块提供给外部的服务接口定义
 * The message-bus module provides the definition of the external service interface
 *
 * @author: Charlie
 */
public interface MessageBusService {

    /**
     * 订阅消息
     * Subscribe to message
     *
     * @param messageClass   需要订阅消息的 class对象
     * @param messageClass   The class object that needs to subscribe to the message.
     * @param messageHandler 消息处理器
     * @param messageHandler The message message
     * @return The id of the subscription message.
     */
    String subscribeMessage(Class<? extends BaseMessage> messageClass, NulsMessageHandler<? extends BaseMessage> messageHandler);


    /**
     * 取消订阅消息
     * unsubscribe
     *
     * @param subscribeId 订阅消息的id.
     * @param subscribeId The id of the message message.
     */
    void unsubscribeMessage(String subscribeId);

    /**
     * 接收消息, 把消息放至消息总线
     * Receive the message and place the message on the message bus.
     *
     * @param message 接收到的消息
     * @param message Received message.
     * @param node    节点, 该消息来自哪个节点.
     * @param node    The message comes as to which node.
     */
    void receiveMessage(BaseMessage message, Node node);

    /**
     * 广播消息
     * broadcast to nodes except "excludeNode"
     *
     * @param message     The message was broadcast.
     * @param excludeNode 不会广播的节点 The node that is not passed.
     * @param aysn        是否异步 Asynchronous execution
     * @return Return all broadcasted node id list
     */
    Result<List<String>> broadcast(BaseMessage message, Node excludeNode, boolean aysn);


    /**
     * 发送消息到一个节点
     * send msg to one node
     *
     * @param message The message you want to sent
     * @param node    The node that received the message
     * @param aysn    是否异步 Asynchronous execution
     * @return Return whether sent successfully
     */
    Result sendToNode(BaseMessage message, Node node, boolean aysn);

    /**
     * 根据消息类型和模块标识实例化一个消息对象
     * Instantiate a message object based on message type and module identity.
     */
    Result<? extends BaseMessage> getMessageInstance(short moduleId, int type);
}
