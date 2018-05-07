package io.nuls.message.bus.service.impl;

import TestHandler.BlockMessageHandler;
import io.nuls.message.bus.handler.intf.NulsMessageHandler;
import io.nuls.message.bus.processor.manager.ProcessorManager;
import io.nuls.message.bus.service.MessageBusService;
import io.nuls.protocol.message.BlockMessage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

public class MessageBusServiceImplTest {

    private MessageBusService messageBusService = new MessageBusServiceImpl();

    @Before
    public void before() {

    }

    @Test
    public void subscribeMessage() throws Exception{
        Class block = BlockMessage.class;
        NulsMessageHandler messageHandler = new BlockMessageHandler();
        String handlerId = messageBusService.subscribeMessage(block, messageHandler);
        Assert.assertNotNull(handlerId);

        Field field = messageBusService.getClass().getDeclaredField("processorManager");
        field.setAccessible(true);
        ProcessorManager processorManager = (ProcessorManager)field.get(messageBusService);

        //验证订阅后handlerMap中的值
        Class processorManagerClass = processorManager.getClass();
        Field handlerMapField = processorManagerClass.getDeclaredField("handlerMap");
        handlerMapField.setAccessible(true);
        Map<String, NulsMessageHandler> handlerMap = (Map<String, NulsMessageHandler>)handlerMapField.get(processorManager);
        Assert.assertNotNull(handlerMap.get(handlerId));
        Assert.assertEquals( handlerMap.get(handlerId), messageHandler);

        //验证订阅后messageHandlerMapping中的值
        Field messageHandlerMappingField = processorManagerClass.getDeclaredField("messageHandlerMapping");
        messageHandlerMappingField.setAccessible(true);
        Map<Class, Set<String>> messageHandlerMapping = (Map<Class, Set<String>>)messageHandlerMappingField.get(processorManager);
        Assert.assertNotNull(messageHandlerMapping.get(block));
        Assert.assertTrue(messageHandlerMapping.get(block).contains(handlerId));
    }

    @Test
    public void unsubscribeMessage() {
    }

    @Test
    public void receiveMessage() {
    }
}