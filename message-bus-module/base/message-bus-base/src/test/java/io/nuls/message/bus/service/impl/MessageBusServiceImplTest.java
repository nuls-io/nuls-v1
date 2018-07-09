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

package io.nuls.message.bus.service.impl;

import TestHandler.BlockMessageHandler;
import io.nuls.kernel.model.Result;
import io.nuls.message.bus.handler.intf.NulsMessageHandler;
import io.nuls.message.bus.manager.DispatchManager;
import io.nuls.message.bus.service.MessageBusService;
import io.nuls.network.model.Node;
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
        DispatchManager processorManager = (DispatchManager) field.get(messageBusService);

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
        DispatchManager processorManager = (DispatchManager) field.get(messageBusService);

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
     * 广播消息
     * broadcast to nodes except "excludeNode"
     */
    @Test
    public void broadcastAndCache() {
        BlockMessage blockMessage = new BlockMessage();
        Node node = new Node("192.168.1.90",8003,1);
        boolean aysn = true;
        Result<List<String>> result =  messageBusService.broadcast(blockMessage, node, aysn,100);
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
