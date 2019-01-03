/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

package io.nuls.network.netty.message.handler;

import io.nuls.kernel.func.TimeService;
import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.Node;
import io.nuls.network.netty.manager.NodeManager;
import io.nuls.network.protocol.handler.BaseNetworkMeesageHandler;
import io.nuls.network.protocol.message.NetworkMessageBody;
import io.nuls.network.protocol.message.VersionMessage;
import io.nuls.protocol.message.base.BaseMessage;

public class VersionMessageHandler implements BaseNetworkMeesageHandler {

    private static VersionMessageHandler instance = new VersionMessageHandler();

    private VersionMessageHandler() {

    }

    public static VersionMessageHandler getInstance() {
        return instance;
    }

    private NodeManager nodeManager = NodeManager.getInstance();

    @Override
    public NetworkEventResult process(BaseMessage message, Node node) {
        VersionMessage versionMessage = (VersionMessage) message;
        NetworkMessageBody body = versionMessage.getMsgBody();

//        Log.info("receive a version message : {}", body);
        if (body.getBestBlockHeight() < 0) {
//            node.setStatus(Node.BAD);
            nodeManager.removeNode(node.getId());
            return null;
        }
        node.setBestBlockHeight(body.getBestBlockHeight());
        node.setBestBlockHash(body.getBestBlockHash());
        node.setTimeOffset((TimeService.currentTimeMillis() - node.getLastTime()) / 2);
        return null;
    }
}
