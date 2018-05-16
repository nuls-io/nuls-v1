package io.nuls.network.protocol.handler;

import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.Node;
import io.nuls.protocol.message.base.BaseMessage;

public interface BaseNetworkMeesageHandler {

    NetworkEventResult process(BaseMessage message, Node node);
}
