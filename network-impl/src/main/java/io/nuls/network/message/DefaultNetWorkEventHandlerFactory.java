/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.network.message;

import io.nuls.core.event.BaseEvent;
import io.nuls.network.message.entity.GetNodeEvent;
import io.nuls.network.message.entity.GetVersionEvent;
import io.nuls.network.message.entity.NodeEvent;
import io.nuls.network.message.entity.VersionEvent;
import io.nuls.network.message.handler.NetWorkEventHandler;
import io.nuls.network.message.impl.GetNodeEventHandler;
import io.nuls.network.message.impl.GetVersionEventHandler;
import io.nuls.network.message.impl.NodeEventHandler;
import io.nuls.network.message.impl.VersionEventHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class DefaultNetWorkEventHandlerFactory extends NetworkEventHandlerFactory {

    private static final DefaultNetWorkEventHandlerFactory INSTANCE = new DefaultNetWorkEventHandlerFactory();

    private Map<String, NetWorkEventHandler> handlerMap = new HashMap<>();

    private DefaultNetWorkEventHandlerFactory() {
        handlerMap.put(VersionEvent.class.getName(), VersionEventHandler.getInstance());
        handlerMap.put(GetVersionEvent.class.getName(), GetVersionEventHandler.getInstance());
        handlerMap.put(GetNodeEvent.class.getName(), GetNodeEventHandler.getInstance());
        handlerMap.put(NodeEvent.class.getName(), NodeEventHandler.getInstance());
    }

    public static NetworkEventHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public NetWorkEventHandler getHandler(BaseEvent event) {
        return handlerMap.get(event.getClass().getName());
    }
}
