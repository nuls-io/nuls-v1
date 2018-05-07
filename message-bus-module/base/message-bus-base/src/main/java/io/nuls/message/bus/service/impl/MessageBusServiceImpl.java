package io.nuls.message.bus.service.impl;

import io.nuls.message.bus.constant.MessageBusConstant;
import io.nuls.message.bus.handler.intf.NulsMessageHandler;
import io.nuls.message.bus.processor.manager.ProcessorManager;
import io.nuls.message.bus.service.MessageBusService;
import io.nuls.protocol.message.base.BaseMessage;

/**
 * @author: Charlie
 * @date: 2018/5/6
 */
public class MessageBusServiceImpl implements MessageBusService {

    private ProcessorManager processorManager = ProcessorManager.getInstance();

    @Override
    public String subscribeMessage(Class<? extends BaseMessage> messageClass, NulsMessageHandler<? extends BaseMessage> messageHandler) {

        return processorManager.registerEventHandler(null, messageClass, messageHandler);
    }

    @Override
    public void unsubscribeMessage(String subscribeId) {

    }

    @Override
    public void receiveMessage(BaseMessage message, String fromId) {

    }
}
