package io.nuls.network.message.entity;

import io.nuls.core.constant.NulsConstant;
import io.nuls.core.event.BaseNetworkEvent;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Node;

import java.util.List;

/**
 * @author vivi
 * @date 2017/12/5.
 */
public class NodeEvent extends BaseNetworkEvent<NodeEventBody> {

    public static final short OWN_MAIN_VERSION = 1;

    public static final short OWN_SUB_VERSION = 1001;

    public NodeEvent() {
        super(NulsConstant.MODULE_ID_NETWORK, NetworkConstant.NETWORK_NODE_EVENT);
     //   this.version = new NulsVersion(OWN_MAIN_VERSION, OWN_SUB_VERSION);
    }

    public NodeEvent(List<Node> nodes) {
        super(NulsConstant.MODULE_ID_NETWORK, NetworkConstant.NETWORK_NODE_EVENT);
     //   this.version = new NulsVersion(OWN_MAIN_VERSION, OWN_SUB_VERSION);
        this.setEventBody(new NodeEventBody(nodes));
    }

    @Override
    protected NodeEventBody parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {
        return byteBuffer.readNulsData(new NodeEventBody());
    }

}
