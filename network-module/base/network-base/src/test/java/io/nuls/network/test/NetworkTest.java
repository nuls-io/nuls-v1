package io.nuls.network.test;

import io.nuls.kernel.model.NulsDigestData;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.protocol.message.GetNodesIpMessage;
import io.nuls.network.protocol.message.HandshakeMessage;
import io.nuls.network.protocol.message.NetworkMessageBody;
import io.nuls.network.protocol.message.NodeMessageBody;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class NetworkTest {

    @Test
    public void testNetworkMessage() {

        try {
//            NetworkMessageBody body = new NetworkMessageBody(NetworkConstant.HANDSHAKE_SEVER_TYPE, 4567,
//                    10001, NulsDigestData.calcDigestData ("a1b2c3d4e5gf6g7h8i9j10".getBytes()));
//            HandshakeMessage handshakeMessage = new HandshakeMessage(body);
//            System.out.println(handshakeMessage.getMsgBody().getBestBlockHash().getDigestHex());
//            HandshakeMessage handshakeMessage1 = new HandshakeMessage();
//            handshakeMessage1.parse(handshakeMessage.serialize());
//            System.out.println(handshakeMessage1.getMsgBody().getBestBlockHash().getDigestHex());

            List<String> list = new ArrayList<>();
            list.add("adsda");
            list.add("adsda");
            list.add("adsda");
            list.add("adsda");
            list.add("adsda");
            NodeMessageBody nodeMessageBody = new NodeMessageBody();
            nodeMessageBody.setIpList(list);
            GetNodesIpMessage getNodesIpMessage = new GetNodesIpMessage(nodeMessageBody);
            getNodesIpMessage.serialize();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
