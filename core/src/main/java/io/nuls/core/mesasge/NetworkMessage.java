package io.nuls.core.mesasge;

import io.nuls.core.mesasge.constant.MessageTypeEnum;

/**
 * Created by Niels on 2017/11/10.
 * nuls.io
 */
public class NetworkMessage extends NulsMessage {

    public NetworkMessage(byte[] bytes) {
        super(MessageTypeEnum.NETWORK, bytes);
    }
}
