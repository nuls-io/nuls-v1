package io.nuls.message.bus.module;

import io.nuls.message.bus.service.MessageBusService;
import io.nuls.message.bus.service.impl.MessageBusServiceImpl;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import static org.junit.Assert.*;

public class MessageBusModuleBootstrapTest {

    private MessageBusModuleBootstrap messageBusModuleBootstrap = new MessageBusModuleBootstrap();

    /**
     * 向MessageBusModuleBootstrap 注入实例化MessageBusService对象
     * @throws Exception
     */
    @Before
    public void before() throws Exception{
        MessageBusService messageBusService = new MessageBusServiceImpl();
        Class processorManagerClass = messageBusModuleBootstrap.getClass();
        Field messageBusServiceField = processorManagerClass.getDeclaredField("messageBusService");
        messageBusServiceField.setAccessible(true);
        assertNull(messageBusServiceField.get(messageBusModuleBootstrap));
        messageBusServiceField.set(messageBusModuleBootstrap, messageBusService);
        assertNotNull(messageBusServiceField.get(messageBusModuleBootstrap));
    }

    @Test
    public void start() {
        messageBusModuleBootstrap.start();
    }

    @Test
    public void shutdown() {
        messageBusModuleBootstrap.shutdown();
    }

    @Test
    public void destroy() {
        messageBusModuleBootstrap.destroy();
    }
}