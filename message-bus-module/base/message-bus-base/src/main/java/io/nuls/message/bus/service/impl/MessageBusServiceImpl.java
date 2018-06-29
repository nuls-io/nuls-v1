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

package io.nuls.message.bus.service.impl;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.Result;
import io.nuls.message.bus.constant.MessageBusErrorCode;
import io.nuls.message.bus.manager.DispatchManager;
import io.nuls.message.bus.manager.HandlerManager;
import io.nuls.message.bus.handler.intf.NulsMessageHandler;
import io.nuls.message.bus.manager.MessageManager;
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
 */
@Service
public class MessageBusServiceImpl implements MessageBusService {

    @Autowired
    private NetworkService networkService;
    private HandlerManager handlerManager = HandlerManager.getInstance();
    private DispatchManager processorManager = DispatchManager.getInstance();

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
    public Result<List<String>> broadcast(BaseMessage message, Node excludeNode, boolean aysn) {
        BroadcastResult result = networkService.sendToAllNode(message, excludeNode, aysn);
        return getNodeIdListResult(result);
    }

    @Override
    public Result sendToNode(BaseMessage message, Node node, boolean aysn) {
        BroadcastResult result = networkService.sendToNode(message, node, aysn);
        if (!result.isSuccess()) {
            Log.error("send to node fail reason: " + result.getErrorCode().getMsg() + "::::" + node.getId());
        }

        return new Result(result.isSuccess(), result.getErrorCode(), null);
    }

    @Override
    public Result<? extends BaseMessage> getMessageInstance(short moduleId, int type) {
        Class<? extends BaseMessage> clazz = MessageManager.getMessage(moduleId, type);
        if (null == clazz) {
            return Result.getFailed(MessageBusErrorCode.UNKOWN_MSG_TYPE);
        }
        BaseMessage message = null;
        try {
            message = clazz.newInstance();
        } catch (InstantiationException e) {
            Log.error(e);
            return Result.getFailed(MessageBusErrorCode.INSTANTIATION_EXCEPTION);
        } catch (IllegalAccessException e) {
            Log.error(e);
            return Result.getFailed(MessageBusErrorCode.ILLEGAL_ACCESS_EXCEPTION);
        }

        return Result.getSuccess().setData(message);
    }

    private Result<List<String>> getNodeIdListResult(BroadcastResult result) {
        List<String> list = new ArrayList<>();
        if (!result.isSuccess() || result.getBroadcastNodes() == null || result.getBroadcastNodes().isEmpty()) {
            return Result.getFailed(result.getErrorCode()).setData(list);
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
