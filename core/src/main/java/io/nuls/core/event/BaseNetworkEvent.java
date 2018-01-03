package io.nuls.core.event;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.NulsSignData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/11/7
 */
public abstract class BaseNetworkEvent<T extends BaseNulsData> extends BaseEvent<T> {


    public BaseNetworkEvent(short moduleId, short eventType, byte[] extend) {
        super(moduleId, eventType, extend);
    }

    public BaseNetworkEvent(short moduleId, short eventType) {
        this(moduleId, eventType, null);
    }

}
