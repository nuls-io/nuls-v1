package io.nuls.network.message.impl;

import io.nuls.kernel.func.TimeService;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.entity.NetworkEventResult;
import io.nuls.network.entity.Node;
import io.nuls.network.protocol.handler.BaseNetworkMeesageHandler;
import io.nuls.network.protocol.message.NodeMessageBody;
import io.nuls.network.protocol.message.NodesIpMessage;
import io.nuls.protocol.message.base.BaseMessage;

public class NodesIpMessageHandler implements BaseNetworkMeesageHandler {

    private static NodesIpMessageHandler instance = new NodesIpMessageHandler();

    private NodesIpMessageHandler() {

    }

    public static NodesIpMessageHandler getInstance() {
        return instance;
    }

    private NetworkParam networkParam = NetworkParam.getInstance();

    @Override
    public NetworkEventResult process(BaseMessage message, Node node) {
        System.out.println("---------------------NodesIpMessageHandler process----------------------");
        NodesIpMessage handshakeMessage = (NodesIpMessage) message;
        NodeMessageBody body = handshakeMessage.getMsgBody();

        for(String ip : body.getIpList()) {
            networkParam.getIpMap().put(ip, TimeService.currentTimeMillis());
        }
        return null;
    }
}
