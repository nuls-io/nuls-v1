package io.nuls.message.bus.module;

import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.message.bus.handler.CommonDigestHandler;
import io.nuls.message.bus.handler.GetMessageBodyHandler;
import io.nuls.message.bus.message.CommonDigestMessage;
import io.nuls.message.bus.message.GetMessageBodyMessage;
import io.nuls.message.bus.processor.manager.ProcessorManager;
import io.nuls.message.bus.service.MessageBusService;
import io.nuls.message.bus.service.impl.MessageCacheService;

/**
 * @author: Charlie
 * @date: 2018/5/6
 */
public class MessageBusModuleBootstrap extends AbstractMessageBusModule {


    @Override
    public void init() {

    }

    @Override
    public void start() {

        MessageBusService messageBusService = NulsContext.getServiceBean(MessageBusService.class);
        messageBusService.subscribeMessage(CommonDigestMessage.class, new CommonDigestHandler());
        messageBusService.subscribeMessage(GetMessageBodyMessage.class, new GetMessageBodyHandler());
    }

    @Override
    public void shutdown() {
        ProcessorManager.getInstance().shutdown();
    }

    @Override
    public void destroy() {
        MessageCacheService.getInstance().destroy();
    }

    @Override
    public String getInfo() {
        return null;
    }
}
