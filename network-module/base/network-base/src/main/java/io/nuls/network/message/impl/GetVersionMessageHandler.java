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

package io.nuls.network.message.impl;

import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.func.TimeService;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.Node;
import io.nuls.network.manager.NodeManager;
import io.nuls.network.protocol.handler.BaseNetworkMeesageHandler;
import io.nuls.network.protocol.message.GetVersionMessage;
import io.nuls.network.protocol.message.NetworkMessageBody;
import io.nuls.network.protocol.message.VersionMessage;
import io.nuls.protocol.message.base.BaseMessage;

public class GetVersionMessageHandler implements BaseNetworkMeesageHandler {

    private static GetVersionMessageHandler instance = new GetVersionMessageHandler();

    private GetVersionMessageHandler() {

    }

    public static GetVersionMessageHandler getInstance() {
        return instance;
    }

    private NodeManager nodeManager = NodeManager.getInstance();

    private NetworkParam networkParam = NetworkParam.getInstance();

    @Override
    public NetworkEventResult process(BaseMessage message, Node node) {
        GetVersionMessage getVersionMessage = (GetVersionMessage) message;
        NetworkMessageBody body = getVersionMessage.getMsgBody();

        if (body.getBestBlockHeight() < 0) {
            node.setStatus(Node.BAD);
            nodeManager.removeNode(node.getId());
            return null;
        }
        node.setBestBlockHeight(body.getBestBlockHeight());
        node.setBestBlockHash(body.getBestBlockHash());

        NetworkMessageBody myVersionBody = new NetworkMessageBody(NetworkConstant.HANDSHAKE_CLIENT_TYPE, networkParam.getPort(),
                NulsContext.getInstance().getBestHeight(), NulsContext.getInstance().getBestBlock().getHeader().getHash());
        return new NetworkEventResult(true, new VersionMessage(myVersionBody));
    }

}
