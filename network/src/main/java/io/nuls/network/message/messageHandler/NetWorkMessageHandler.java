package io.nuls.network.message.messageHandler;

import io.nuls.network.entity.Peer;
import io.nuls.network.message.NetworkMessage;
import io.nuls.network.message.NetworkMessageResult;

/**
 * Created by vivi on 2017/11/21.
 */
public interface NetWorkMessageHandler {


    NetworkMessageResult process(NetworkMessage message , Peer peer);
}
