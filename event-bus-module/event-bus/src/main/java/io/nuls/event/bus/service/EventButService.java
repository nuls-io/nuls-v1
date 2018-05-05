package io.nuls.event.bus.service;

import io.nuls.event.bus.handler.intf.NulsEventHandler;
import io.nuls.event.bus.model.EventItem;
import io.nuls.protocol.message.base.BaseMessage;

import java.util.List;

/**
 *  事件总线模块提供给外部的服务接口定义
 *  The event-bus module provides the definition of the external service interface
 * @author: Charlie
 * @date: 2018/5/4
 */
public interface EventButService {

    String subscribeEvent(Class<? extends BaseMessage> eventClass, NulsEventHandler<? extends BaseMessage> eventHandler);

    void unsubscribeEvent(String subcribeId);

    void receiveMessage(BaseMessage message, String fromId);

}
