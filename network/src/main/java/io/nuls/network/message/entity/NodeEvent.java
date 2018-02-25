/**
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
 */
package io.nuls.network.message.entity;

import io.nuls.core.chain.entity.NulsVersion;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.event.BaseEvent;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author vivi
 * @date 2017/12/5.
 */
public class NodeEvent extends BaseEvent<NodeEventBody> {

    public static final short OWN_MAIN_VERSION = 1;

    public static final short OWN_SUB_VERSION = 1001;

    public NodeEvent() {
        super(NulsConstant.MODULE_ID_NETWORK, NetworkConstant.NETWORK_NODE_EVENT);
//        this.version = new NulsVersion(OWN_MAIN_VERSION, OWN_SUB_VERSION);
    }

    public NodeEvent(List<Node> nodes) {
        this();
        this.setEventBody(new NodeEventBody(nodes));
    }

    @Override
    protected NodeEventBody parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {

        return byteBuffer.readNulsData(new NodeEventBody());
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("NodeEvent:{");
        buffer.append(getEventBody().toString() + "}");
        return buffer.toString();
    }

    public static void main(String[] args) throws IOException, NulsException {
        List<Node> nodes = new ArrayList<>();
        Node node1 = new Node();
        node1.setIp("192.168.1.111");
        node1.setPort(8003);
        node1.setMagicNumber(123456789);
        nodes.add(node1);

        Node node2 = new Node();
        node2.setIp("192.168.1.222");
        node2.setPort(8005);
        node2.setMagicNumber(1234567339);
        nodes.add(node2);

        NodeEvent nodeEvent = new NodeEvent(nodes);
        System.out.println(nodeEvent);

        byte[] bytes = nodeEvent.serialize();
        NodeEvent event = new NodeEvent();
        event.parse(new NulsByteBuffer(bytes));
        System.out.println(event);
    }

}
