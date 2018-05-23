package io.nuls.message.bus.service.impl;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.Result;
import io.nuls.message.bus.manager.DispatchManager;
import io.nuls.message.bus.manager.HandlerManager;
import io.nuls.message.bus.handler.intf.NulsMessageHandler;
import io.nuls.message.bus.manager.MessageManager;
import io.nuls.message.bus.message.CommonDigestMessage;
import io.nuls.message.bus.model.ProcessData;
import io.nuls.message.bus.service.MessageBusService;
import io.nuls.network.model.BroadcastResult;
import io.nuls.network.model.Node;
import io.nuls.network.service.NetworkService;
import io.nuls.protocol.message.base.BaseMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Charlie
 * @date: 2018/5/6
 */
@Service
public class MessageBusServiceImpl implements MessageBusService {

    @Autowired
    private NetworkService networkService;
    private HandlerManager handlerManager = HandlerManager.getInstance();
    private DispatchManager processorManager = DispatchManager.getInstance();
    private MessageCacheService messageCacheService = MessageCacheService.getInstance();

    @Override
    public String subscribeMessage(Class<? extends BaseMessage> messageClass, NulsMessageHandler<? extends BaseMessage> messageHandler) {
        MessageManager.putMessage(messageClass);
        return handlerManager.registerMessageHandler(null, messageClass, messageHandler);
    }

    @Override
    public void unsubscribeMessage(String subscribeId) {
        this.handlerManager.removeMessageHandler(subscribeId);
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
        messageCacheService.cacheSendedMessage(message);
        BroadcastResult result = this.networkService.sendToAllNode(new CommonDigestMessage(message.getHash()), aysn);
        return getNodeIdListResult(result);
    }

    @Override
    public Result<List<String>> broadcastAndCache(BaseMessage message, Node excludeNode, boolean aysn) {
        messageCacheService.cacheSendedMessage(message);
        BroadcastResult result = networkService.sendToAllNode(message, excludeNode, aysn);
        return getNodeIdListResult(result);
    }

    @Override
    public Result sendToNode(BaseMessage message, Node node, boolean aysn) {
        BroadcastResult result = networkService.sendToNode(message, node, aysn);
        if (!result.isSuccess()) {
            Log.error("send to node fail reason: " + result.getErrorCode().getMsg());
        }

        return new Result(result.isSuccess(), result.getErrorCode(), null);
    }

    @Override
    public Result<? extends BaseMessage> getMessageInstance(short moduleId, int type) {
        Class<? extends BaseMessage> clazz = MessageManager.getMessage(moduleId, type);
        if (null == clazz) {
            return Result.getFailed("the message type can not found!");
        }
        BaseMessage message = null;
        try {
            message = clazz.newInstance();
        } catch (InstantiationException e) {
            Log.error(e);
            return Result.getFailed(e.getMessage());
        } catch (IllegalAccessException e) {
            Log.error(e);
            return Result.getFailed(e.getMessage());
        }

        return Result.getSuccess().setData(message);
    }

    private Result<List<String>> getNodeIdListResult(BroadcastResult result) {
        List<String> list = new ArrayList<>();
        if (!result.isSuccess() || result.getBroadcastNodes() == null || result.getBroadcastNodes().isEmpty()) {
            return Result.getFailed().setData(list);
        }
        for (Node node : result.getBroadcastNodes()) {
            list.add(node.getId());
        }
        Result rs = new Result();
        rs.setSuccess(true);
        rs.setData(list);
        return rs;
    }
}
