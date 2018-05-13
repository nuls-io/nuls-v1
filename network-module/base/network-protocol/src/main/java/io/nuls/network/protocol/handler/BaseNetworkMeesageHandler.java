package io.nuls.network.protocol.handler;

import io.nuls.network.entity.NetworkEventResult;
import io.nuls.network.entity.Node;
import io.nuls.protocol.message.base.BaseMessage;

public interface BaseNetworkMeesageHandler {

    NetworkEventResult process(BaseMessage message, Node node);
}
