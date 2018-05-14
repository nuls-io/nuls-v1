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
package io.nuls.protocol.base.module;


import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.thread.manager.TaskManager;
import io.nuls.message.bus.constant.MessageBusConstant;
import io.nuls.message.bus.service.MessageBusService;
import io.nuls.protocol.base.handler.*;
import io.nuls.protocol.base.service.DownloadServiceImpl;
import io.nuls.protocol.message.*;
import io.nuls.protocol.module.AbstractProtocolModule;
import io.nuls.protocol.service.DownloadService;

;

/**
 * @author Niels
 * @date 2017/11/7
 */
public class BaseProtocolsModuleBootstrap extends AbstractProtocolModule {


    @Override
    public void init() {
    }

    @Override
    public void start() {
        this.waitForDependencyRunning(MessageBusConstant.MODULE_ID_MESSAGE_BUS);
        this.initHandlers();
        ((DownloadServiceImpl) NulsContext.getServiceBean(DownloadService.class)).start();
    }

    private void initHandlers() {
        MessageBusService messageBusService = NulsContext.getServiceBean(MessageBusService.class);
        messageBusService.subscribeMessage(BlockMessage.class, new BlockMessageHandler());
        messageBusService.subscribeMessage(BlocksHashMessage.class, new BlocksHashHandler());
        messageBusService.subscribeMessage(GetBlocksHashRequest.class, new GetBlocksHashHandler());
        messageBusService.subscribeMessage(NotFoundMessage.class, new NotFoundHander());
        messageBusService.subscribeMessage(GetBlockRequest.class, new GetBlockHandler());
        messageBusService.subscribeMessage(GetTxGroupRequest.class, new GetTxGroupHandler());
        messageBusService.subscribeMessage(TxGroupMessage.class, new TxGroupHandler());
        messageBusService.subscribeMessage(TransactionMessage.class, new NewTxMessageHandler());
        messageBusService.subscribeMessage(SmallBlockMessage.class, new SmallBlockHandler());
    }


    @Override
    public void shutdown() {
        ((DownloadServiceImpl) NulsContext.getServiceBean(DownloadService.class)).stop();
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
