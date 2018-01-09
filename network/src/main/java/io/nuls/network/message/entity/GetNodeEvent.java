package io.nuls.network.message.entity;

import io.nuls.core.chain.entity.BasicTypeData;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.network.constant.NetworkConstant;

/**
 * @author vivi
 * @date 2017/12/1.
 */
public class GetNodeEvent extends io.nuls.core.event.BaseEvent<BasicTypeData<Integer>> {

    public static final short OWN_MAIN_VERSION = 1;

    public static final short OWN_SUB_VERSION = 1001;

    public GetNodeEvent() {
        super(NulsConstant.MODULE_ID_NETWORK, NetworkConstant.NETWORK_GET_NODE_EVENT);
//        this.version = new NulsVersion(OWN_MAIN_VERSION, OWN_SUB_VERSION);
    }

    public GetNodeEvent(int length) {
        super(NulsConstant.MODULE_ID_NETWORK, NetworkConstant.NETWORK_GET_NODE_EVENT);
//        this.version = new NulsVersion(OWN_MAIN_VERSION, OWN_SUB_VERSION);
        this.setEventBody(new BasicTypeData<>(length));
    }

    @Override
    protected BasicTypeData<Integer> parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {
        return byteBuffer.readNulsData(new BasicTypeData<>());
    }

}
