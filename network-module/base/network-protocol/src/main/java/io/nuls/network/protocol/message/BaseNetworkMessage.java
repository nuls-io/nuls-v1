package io.nuls.network.protocol.message;

import io.nuls.kernel.model.BaseNulsData;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.protocol.message.base.BaseMessage;
import io.nuls.protocol.model.basic.NulsBytesData;

public class BaseNetworkMessage<T extends BaseNulsData> extends BaseMessage<T> {
    /**
     * 初始化基础消息的消息头
     * @param eventType
     */
    public BaseNetworkMessage(short eventType) {
        super(NetworkConstant.NETWORK_MODULE_ID, eventType);
    }
}
