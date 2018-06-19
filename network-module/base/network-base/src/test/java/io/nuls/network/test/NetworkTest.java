/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.network.test;

import io.nuls.kernel.lite.annotation.Autowired;
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
