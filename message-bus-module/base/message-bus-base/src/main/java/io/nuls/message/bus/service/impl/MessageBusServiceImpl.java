package io.nuls.message.bus.service.impl;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.Result;
import io.nuls.message.bus.handler.intf.NulsMessageHandler;
import io.nuls.message.bus.processor.manager.ProcessData;
import io.nuls.message.bus.processor.manager.ProcessorManager;
import io.nuls.message.bus.service.MessageBusService;
import io.nuls.network.entity.Node;
import io.nuls.protocol.message.base.BaseMessage;

import java.util.List;

/**
 * @author: Charlie
 * @date: 2018/5/6
 */
@Service
public class MessageBusServiceImpl implements MessageBusService {

    private ProcessorManager processorManager = ProcessorManager.getInstance();

    @Override
    public String subscribeMessage(Class<? extends BaseMessage> messageClass, NulsMessageHandler<? extends BaseMessage> messageHandler) {

        return processorManager.registerMessageHandler(null, messageClass, messageHandler);
    }

    @Override
    public void unsubscribeMessage(String subscribeId) {
        this.processorManager.removeMessageHandler(subscribeId);
    }

    @Override
    public void receiveMessage(BaseMessage message, Node node) {
        try {
            this.processorManager.offer(new ProcessData(message, node));
        } catch (Exception e) {
            Log.error(e);
        }

    }
    public void shutdown() {
        this.processorManager.shutdown();
    }

    @Override
    public Result<List<String>> broadcastHashAndCache(BaseMessage message, Node excludeNode, boolean aysn) {
        return null;
    }

    @Override
    public Result<List<String>> broadcastAndCache(BaseMessage message, Node excludeNode, boolean aysn) {
        return null;
    }

    @Override
    public Result sendToNode(BaseMessage message, String nodeId, boolean aysn) {
        return null;
    }
}
