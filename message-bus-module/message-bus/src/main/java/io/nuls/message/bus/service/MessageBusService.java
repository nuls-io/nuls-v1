package io.nuls.message.bus.service;

import io.nuls.message.bus.message.intf.NulsMessageHandler;
import io.nuls.protocol.message.base.BaseMessage;

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
     * @param fromId  节点id, 该消息来自哪个节点.
     * @param fromId  The node's id, The message comes as to which node.
     */
    void receiveMessage(BaseMessage message, String fromId);

}