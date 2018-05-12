package io.nuls.network.test;

import io.nuls.kernel.model.NulsDigestData;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.protocol.message.HandshakeMessage;
import io.nuls.network.protocol.message.NetworkMessageBody;
import org.junit.Test;

public class NetworkTest {

    @Test
    public void testNetworkMessage() {

        try {
            NetworkMessageBody body = new NetworkMessageBody(NetworkConstant.HANDSHAKE_SEVER_TYPE, 4567,
                    10001, NulsDigestData.calcDigestData ("a1b2c3d4e5gf6g7h8i9j10".getBytes()));
            HandshakeMessage handshakeMessage = new HandshakeMessage(body);
            System.out.println(handshakeMessage.getMsgBody().getBestBlockHash().getDigestHex());
            HandshakeMessage handshakeMessage1 = new HandshakeMessage();
            handshakeMessage1.parse(handshakeMessage.serialize());
            System.out.println(handshakeMessage1.getMsgBody().getBestBlockHash().getDigestHex());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
