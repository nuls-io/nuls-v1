package io.nuls.core.mesasge;

import io.nuls.core.mesasge.constant.MessageTypeEnum;

/**
 * Created by Niels on 2017/11/10.
 *
 */
public class NetworkMessage extends NulsMessage {

    public NetworkMessage(NulsMessageHeader header, byte[] data) {
        super(header, data);
    }
}
