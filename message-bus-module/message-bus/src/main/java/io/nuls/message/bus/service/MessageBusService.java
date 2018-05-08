package io.nuls.message.bus.service;

import io.nuls.kernel.model.Result;
import io.nuls.message.bus.handler.intf.NulsMessageHandler;
import io.nuls.network.entity.Node;
import io.nuls.protocol.message.base.BaseMessage;

import java.util.List;

/**
 * 消息总线模块提供给外部的服务接口定义
 * The event-bus module provides the definition of the external service interface
 *
 * @author: Charlie
 * @date: 2018/5/4
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
     * 广播消息hash
     * broadcast a message hash and cache that need to be passed
     *
     * @param message     The message was broadcast.
     * @param excludeNode 不会广播的节点 The node that is not passed.
     * @param aysn        是否异步 Asynchronous execution
     * @return Return all broadcasted node id list
     */
    Result<List<String>> broadcastHashAndCache(BaseMessage message, Node excludeNode, boolean aysn);

    /**
     * 广播消息
     * broadcast to nodes except "excludeNodeId"
     *
     * @param message     The message was broadcast.
     * @param excludeNode 不会广播的节点 The node that is not passed.
     * @param aysn        是否异步 Asynchronous execution
     * @return Return all broadcasted node id list
     */
    Result<List<String>> broadcastAndCache(BaseMessage message, Node excludeNode, boolean aysn);


    /**
     * 发送消息到一个节点
     * send msg to one node
     *
     * @param message The message you want to sent
     * @param nodeId  The node id that received the message
     * @param aysn    是否异步 Asynchronous execution
     * @return Return whether sent successfully
     */
    Result sendToNode(BaseMessage message, Node Node, boolean aysn);


}