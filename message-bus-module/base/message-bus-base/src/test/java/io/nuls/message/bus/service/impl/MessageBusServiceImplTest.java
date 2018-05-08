package io.nuls.message.bus.service.impl;

import TestHandler.BlockMessageHandler;
import io.nuls.message.bus.handler.intf.NulsMessageHandler;
import io.nuls.message.bus.processor.manager.ProcessorManager;
import io.nuls.message.bus.service.MessageBusService;
import io.nuls.protocol.message.BlockMessage;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class MessageBusServiceImplTest {

    private MessageBusService messageBusService = new MessageBusServiceImpl();

    private NulsMessageHandler messageHandler = null;

    private String handlerId = null;

    private Class block = null;

    @Before
    public void before() {
        subscribe();
    }

    private void subscribe(){
        block = BlockMessage.class;
        NulsMessageHandler messageHandler = new BlockMessageHandler();
        handlerId = messageBusService.subscribeMessage(block, messageHandler);
    }

    /**
     * 验证订阅后的返回值
     * 验证订阅后handlerMap中的值
     * 验证订阅后messageHandlerMapping中的值
     * Validate the subscription's return value
     * Verify the value of the handlerMap & messageHandlerMapping after subscribing
     */
    @Test
    public void subscribeMessage() throws Exception {
        subscribe();
        assertNotNull(handlerId);

        Field field = messageBusService.getClass().getDeclaredField("processorManager");
        field.setAccessible(true);
        ProcessorManager processorManager = (ProcessorManager) field.get(messageBusService);

        //验证订阅后handlerMap中的值
        Class processorManagerClass = processorManager.getClass();
        Field handlerMapField = processorManagerClass.getDeclaredField("handlerMap");
        handlerMapField.setAccessible(true);
        Map<String, NulsMessageHandler> handlerMap = (Map<String, NulsMessageHandler>) handlerMapField.get(processorManager);
        assertNotNull(handlerMap.get(handlerId));
        assertEquals(handlerMap.get(handlerId), messageHandler);

        //验证订阅后messageHandlerMapping中的值
        Field messageHandlerMappingField = processorManagerClass.getDeclaredField("messageHandlerMapping");
        messageHandlerMappingField.setAccessible(true);
        Map<Class, Set<String>> messageHandlerMapping = (Map<Class, Set<String>>) messageHandlerMappingField.get(processorManager);
        assertNotNull(messageHandlerMapping.get(block));
        assertTrue(messageHandlerMapping.get(block).contains(handlerId));
    }

    @Test
    public void unsubscribeMessage() throws Exception {
        messageBusService.unsubscribeMessage(handlerId);

        Field field = messageBusService.getClass().getDeclaredField("processorManager");
        field.setAccessible(true);
        ProcessorManager processorManager = (ProcessorManager) field.get(messageBusService);

        //验证取消订阅后handlerMap中的值
        Class processorManagerClass = processorManager.getClass();
        Field handlerMapField = processorManagerClass.getDeclaredField("handlerMap");
        handlerMapField.setAccessible(true);
        Map<String, NulsMessageHandler> handlerMap = (Map<String, NulsMessageHandler>) handlerMapField.get(processorManager);
        assertNull(handlerMap.get(handlerId));

    }

    @Test
    public void receiveMessage() {
        BlockMessage blockMessage = new BlockMessage();
        String nodeId = "192.168.1.90:8003";
        messageBusService.receiveMessage(blockMessage, nodeId);
    }


}