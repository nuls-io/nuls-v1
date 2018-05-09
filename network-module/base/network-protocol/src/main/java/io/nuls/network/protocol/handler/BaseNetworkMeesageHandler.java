package io.nuls.network.protocol.handler;

import io.nuls.network.entity.NetworkEventResult;
import io.nuls.network.entity.Node;
import io.nuls.network.protocol.message.BaseNetworkMessage;

public interface BaseNetworkMeesageHandler {

    NetworkEventResult process(BaseNetworkMessage message, Node node);
}
