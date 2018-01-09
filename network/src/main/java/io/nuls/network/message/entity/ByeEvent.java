package io.nuls.network.message.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.NulsVersion;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.log.Log;
import io.nuls.network.constant.NetworkConstant;

/**
 * @author vivi
 * @Date 2017.11.01
 */
public class ByeEvent extends io.nuls.core.event.BaseEvent {
    public static final short OWN_MAIN_VERSION = 1;
    public static final short OWN_SUB_VERSION = 0001;

    public ByeEvent() {
        super(NulsConstant.MODULE_ID_NETWORK, NetworkConstant.NETWORK_BYE_EVENT);
        this.version = new NulsVersion(OWN_MAIN_VERSION, OWN_SUB_VERSION);
    }

    @Override
    protected BaseNulsData parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {
        return null;
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
