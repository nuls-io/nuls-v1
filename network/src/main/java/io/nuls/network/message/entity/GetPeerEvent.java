package io.nuls.network.message.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.BasicTypeData;
import io.nuls.core.chain.entity.NulsVersion;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.event.BaseNetworkEvent;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.log.Log;
import io.nuls.network.constant.NetworkConstant;

import java.io.IOException;

/**
 * @author vivi
 * @date 2017/12/1.
 */
public class GetPeerEvent extends BaseNetworkEvent<BasicTypeData<Integer>> {

    public static final short OWN_MAIN_VERSION = 1;

    public static final short OWN_SUB_VERSION = 1001;


    public GetPeerEvent() {
        super(NulsConstant.MODULE_ID_NETWORK, NetworkConstant.NETWORK_GET_PEER_MESSAGE);
       // this.version = new NulsVersion(OWN_MAIN_VERSION, OWN_SUB_VERSION);
    }

    public GetPeerEvent(int length) {
        super(NulsConstant.MODULE_ID_NETWORK, NetworkConstant.NETWORK_GET_PEER_MESSAGE);
      //  this.version = new NulsVersion(OWN_MAIN_VERSION, OWN_SUB_VERSION);
        this.setEventBody(new BasicTypeData<>(length));
    }

    @Override
    protected BasicTypeData<Integer> parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {
        return byteBuffer.readNulsData(new BasicTypeData<>());
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


    public static void main(String[] args) throws IOException, NulsException {
        GetPeerEvent event = new GetPeerEvent(20);
        byte[] bytes = event.serialize();

        GetPeerEvent event1 = new GetPeerEvent();
        event1.parse( bytes );
        System.out.println(event1.getEventBody().getVal());

    }

}
