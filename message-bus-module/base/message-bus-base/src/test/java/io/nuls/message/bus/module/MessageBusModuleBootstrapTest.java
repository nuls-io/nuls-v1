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
