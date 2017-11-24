package io.nuls.network.message.messageHandler;

import io.nuls.network.entity.Peer;
import io.nuls.network.message.AbstractNetworkMessage;
import io.nuls.network.message.NetworkMessageResult;

import java.io.IOException;

/**
 *
 * @author vivi
 * @date 2017/11/21
 */
public interface NetWorkMessageHandler {


    NetworkMessageResult process(AbstractNetworkMessage message , Peer peer);
}
