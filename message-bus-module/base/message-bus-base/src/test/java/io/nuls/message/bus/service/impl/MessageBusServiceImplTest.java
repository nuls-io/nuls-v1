package io.nuls.message.bus.service.impl;

import TestHandler.BlockMessageHandler;
import io.nuls.kernel.model.Result;
import io.nuls.message.bus.handler.intf.NulsMessageHandler;
import io.nuls.message.bus.processor.manager.ProcessorManager;
import io.nuls.message.bus.service.MessageBusService;
import io.nuls.network.entity.Node;
import io.nuls.network.service.NetworkService;
import io.nuls.network.service.impl.NetworkServiceImpl;
import io.nuls.protocol.message.BlockMessage;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class MessageBusServiceImplTest {

    private MessageBusService messageBusService = new MessageBusServiceImpl();

    private NulsMessageHandler messageHandler = null;

    private String handlerId = null;

    private Class block = null;

    @Before
    public void before() throws Exception{
        subscribe();

        // 向MessageBusModuleBootstrap 注入实例化MessageBusService对象
        NetworkService networkService = new NetworkServiceImpl();
        Field networkServiceField = messageBusService.getClass().getDeclaredField("networkService");
        networkServiceField.setAccessible(true);
        assertNull(networkServiceField.get(messageBusService));
        networkServiceField.set(messageBusService, networkService);
        assertNotNull(networkServiceField.get(messageBusService));

    }

    private void subscribe(){
        block = BlockMessage.class;
        messageHandler = new BlockMessageHandler();
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

    /**
     *
     * 验证取消订阅后handlerMap中的值
     * Verify the value in handlerMap after unsubscribing
     * @throws Exception
     */
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
        Node node = new Node("192.168.1.90",8003,1);
        messageBusService.receiveMessage(blockMessage, node);
    }

    /**
     * 广播消息hash缓存
     * broadcast a message hash and cache that need to be passed
     */
    @Test
    public void broadcastHashAndCache() {
        BlockMessage blockMessage = new BlockMessage();
        Node node = new Node("192.168.1.90",8003,1);
        boolean aysn = true;
        Result<List<String>> result =  messageBusService.broadcastHashAndCache(blockMessage, node, aysn);
        assertTrue(result.isSuccess());
        assertTrue(result.getData().size()>0);
    }

    /**
     * 广播消息
     * broadcast to nodes except "excludeNode"
     */
    @Test
    public void broadcastAndCache() {
        BlockMessage blockMessage = new BlockMessage();
        Node node = new Node("192.168.1.90",8003,1);
        boolean aysn = true;
        Result<List<String>> result =  messageBusService.broadcastAndCache(blockMessage, node, aysn);
        assertTrue(result.isSuccess());
        assertTrue(result.getData().size()>0);
    }

    /**
     * send msg to one node
     */
    @Test
    public void sendToNode() {
        BlockMessage blockMessage = new BlockMessage();
        Node node = new Node("192.168.1.90",8003,1);
        boolean aysn = true;
        assertTrue(messageBusService.sendToNode(blockMessage, node, aysn).isSuccess());
    }
}