package io.nuls.consensus.event;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.event.BaseNetworkEvent;
import io.nuls.core.utils.log.Log;

/**
 * @author Niels
 * @date 2017/11/7
 */
public abstract class BaseConsensusEvent<T extends BaseNulsData> extends BaseNetworkEvent<T> {

    public BaseConsensusEvent(short eventType) {
        super(NulsConstant.MODULE_ID_CONSENSUS, eventType);
    }

    @Override
    public Object copy() {
        try {
            return this.clone();
        } catch (CloneNotSupportedException e) {
            Log.error(e);
            return null;
        }
    }
}