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
 */
package io.nuls.protocol.base.module.impl;


import io.nuls.consensus.poc.protocol.service.DownloadService;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.service.intf.EventBusService;
import io.nuls.protocol.base.download.DownloadServiceImpl;
import io.nuls.protocol.base.handler.BlockEventHandler;
import io.nuls.protocol.base.handler.BlocksHashHandler;
import io.nuls.protocol.base.handler.GetBlocksHashHandler;
import io.nuls.protocol.base.handler.NotFoundHander;
import io.nuls.protocol.base.service.impl.BlockServiceImpl;
import io.nuls.protocol.base.service.impl.SystemServiceImpl;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.event.BlockEvent;
import io.nuls.protocol.event.BlocksHashEvent;
import io.nuls.protocol.event.GetBlocksHashRequest;
import io.nuls.protocol.event.NotFoundEvent;
import io.nuls.protocol.module.AbstractProtocolModule;

/**
 * @author Niels
 * @date 2017/11/7
 */
public class BaseProtocolsModuleBootstrap extends AbstractProtocolModule {

    private EventBusService eventBusService = NulsContext.getServiceBean(EventBusService.class);

    @Override
    public void init() {
        this.registerService(BlockServiceImpl.class);
        this.registerService(DownloadServiceImpl.class);
        this.registerService(SystemServiceImpl.class);
    }

    @Override
    public void start() {
        this.initHandlers();
        NulsContext.getServiceBean(DownloadService.class).start();
        Log.info("the protocol module is started!");
    }

    private void initHandlers() {
        BlockEventHandler blockEventHandler = new BlockEventHandler();
        eventBusService.subscribeEvent(BlockEvent.class, blockEventHandler);
        eventBusService.subscribeEvent(BlocksHashEvent.class, new BlocksHashHandler());
        eventBusService.subscribeEvent(GetBlocksHashRequest.class, new GetBlocksHashHandler());

        eventBusService.subscribeEvent(NotFoundEvent.class, new NotFoundHander());
    }


    @Override
    public void shutdown() {
        TaskManager.shutdownByModuleId(this.getModuleId());
    }

    @Override
    public void destroy() {
    }

    @Override
    public String getInfo() {

        return "";
    }

}
