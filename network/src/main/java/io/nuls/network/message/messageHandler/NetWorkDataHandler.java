package io.nuls.network.message.messageHandler;

import io.nuls.network.entity.Peer;
import io.nuls.network.message.BaseNetworkData;
import io.nuls.network.message.NetworkDataResult;

/**
 *
 * @author vivi
 * @date 2017/11/21
 */
public interface NetWorkDataHandler {


    NetworkDataResult process(BaseNetworkData message , Peer peer);
}
