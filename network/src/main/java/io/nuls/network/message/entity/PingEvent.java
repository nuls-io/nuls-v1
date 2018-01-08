package io.nuls.network.message.entity;

import io.nuls.core.chain.entity.BasicTypeData;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.event.BaseNetworkEvent;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.network.constant.NetworkConstant;


/**
 * @author vivi
 * @Date 2017.11.01
 */
public class PingEvent extends BaseNetworkEvent {
    public static final short OWN_MAIN_VERSION = 1;
    public static final short OWN_SUB_VERSION = 0001;

    public PingEvent() {
        super(NulsConstant.MODULE_ID_NETWORK, NetworkConstant.NETWORK_PING_EVENT);
    }

    @Override
    protected BasicTypeData<String> parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {
        return null;
    }
}
