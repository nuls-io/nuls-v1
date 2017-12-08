package io.nuls.consensus.event;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.event.BaseNulsEvent;
import io.nuls.core.event.NulsEventHeader;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * @author Niels
 * @date 2017/11/7
 */
public abstract class BaseConsensusEvent<T extends BaseNulsData> extends BaseNulsEvent<T> {


    public BaseConsensusEvent(short eventType) {
        super(NulsConstant.MODULE_ID_CONSENSUS, eventType);
    }
 
}