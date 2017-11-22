package io.nuls.network.message.messageHandler;

import io.nuls.network.entity.Peer;
import io.nuls.network.message.AbstractNetworkMessage;
import io.nuls.network.message.AbstractNetworkMessageResult;

/**
 * Created by vivi on 2017/11/21.
 */
public interface NetWorkMessageHandler {


    AbstractNetworkMessageResult process(AbstractNetworkMessage message , Peer peer);
}
