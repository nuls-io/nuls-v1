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

package io.nuls.notify.module;

import io.nuls.core.context.NulsContext;
import io.nuls.core.event.BaseEvent;
import io.nuls.core.module.BaseModuleBootstrap;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.service.intf.EventBusService;
import io.nuls.notify.controller.NotificationController;
import io.nuls.notify.handler.NulsEventHandler;

/**
 * @author daviyang35
 * @date 2018/1/19
 */
public class NotifyModuleBootstrap extends BaseModuleBootstrap {
    private NotificationController notificationController;
    short port;

    public NotifyModuleBootstrap() {
        super((short) 10);
    }

    @Override
    public void init() {
        Log.debug("Init");
        // read property


        String portString = getModuleCfgProperty("notify", "notify.port");
        if (portString != null) {
            port = Short.parseShort(portString);
        } else {
            port = 8632;
        }
    }

    @Override
    public void start() {
        Log.debug("Start");
        notificationController = new NotificationController();

        EventBusService eventBusService = NulsContext.getServiceBean(EventBusService.class);
        eventBusService.subscribeEvent(BaseEvent.class,notificationController.getNulsEventHandler());

        notificationController.setListenPort(port);
        notificationController.start();
    }

    @Override
    public void shutdown() {
        Log.debug("Shutdown");
        notificationController.stop();
    }

    @Override
    public void destroy() {
        Log.debug("Destroy");
        notificationController = null;
    }

    @Override
    public String getInfo() {
        return null;
    }

    @Override
    public int getVersion() {
        return 0;
    }
}
