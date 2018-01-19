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
package io.nuls.event.bus.module.impl;

import io.nuls.event.bus.constant.EventBusConstant;
import io.nuls.event.bus.event.CommonDigestEvent;
import io.nuls.event.bus.event.GetEventBodyEvent;
import io.nuls.event.bus.service.impl.*;
import io.nuls.event.bus.service.intf.*;
import io.nuls.event.bus.handler.CommonDigestHandler;
import io.nuls.event.bus.handler.GetEventBodyHandler;
import io.nuls.event.bus.module.intf.AbstractEventBusModule;
import io.nuls.network.message.ReplyNotice;

/**
 * @author Niels
 * @date 2017/11/6
 */
public class EventBusModuleBootstrap extends AbstractEventBusModule {

    private EventBusService eventBusService;

    public EventBusModuleBootstrap() {
        super();
    }

    @Override
    public void init() {
        EventCacheService.getInstance().init();
    }

    @Override
    public void start() {
        eventBusService = EventBusServiceImpl.getInstance();
        eventBusService.subscribeEvent(CommonDigestEvent.class, new CommonDigestHandler());
        eventBusService.subscribeEvent(GetEventBodyEvent.class, new GetEventBodyHandler());
        this.registerService(eventBusService);
        this.registerService(EventBroadcaster.class, EventBroadcasterImpl.getInstance());

    }

    @Override
    public void shutdown() {
        LocalEventService.getInstance().shutdown();
        NetworkEventService.getInstance().shutdown();
    }

    @Override
    public void destroy() {
        EventCacheService.getInstance().destroy();
    }

    @Override
    public String getInfo() {
        return null;
    }

    @Override
    public int getVersion() {
        return EventBusConstant.EVENT_BUS_MODULE_VERSION;
    }

}
