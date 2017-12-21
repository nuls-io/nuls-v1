package io.nuls.core.notice;

import io.nuls.core.bus.BaseBusData;
import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.intf.NulsCloneable;
import io.nuls.core.bus.BusDataHeader;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/11/7
 */
public abstract class BaseNulsNotice<T extends BaseNulsData> extends BaseBusData<T>{


    public BaseNulsNotice(short moduleId, short eventType, byte[] extend) {
        super(moduleId, eventType, extend);
    }

    public BaseNulsNotice(short moduleId, short eventType) {
        this(moduleId, eventType,null);
    }
}
