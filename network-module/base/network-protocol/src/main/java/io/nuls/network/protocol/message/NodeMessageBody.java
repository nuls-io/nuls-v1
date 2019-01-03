/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

package io.nuls.network.protocol.message;

import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.NulsOutputStreamBuffer;
import io.nuls.kernel.utils.SerializeUtils;
import io.nuls.kernel.utils.VarInt;
import io.nuls.network.model.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NodeMessageBody extends BaseNulsData {

    private List<Node> nodeList;

    public NodeMessageBody() {

    }

    public NodeMessageBody(List nodeList) {
        this.nodeList = nodeList;
    }

    /**
     * serialize important field
     */
    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {

        int count = nodeList == null ? 0 : nodeList.size();
        stream.writeVarInt(count);
        if (null != nodeList) {
            for (Node node : nodeList) {
                stream.writeNulsData(node);
            }
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {

        List<Node> nodeList = new ArrayList<>();
        int count = (int) byteBuffer.readVarInt();
        for (int i = 0; i < count; i++) {
            nodeList.add(byteBuffer.readNulsData(new Node()));
        }
        this.nodeList = nodeList;
    }

    @Override
    public int size() {
        int s = 0;

        s += SerializeUtils.sizeOfVarInt(nodeList == null ? 0 : nodeList.size());
        if (nodeList != null) {
            for (Node node : nodeList) {
                s += node.size();
            }
        }
        return s;
    }

    public List<Node> getNodeList() {
        return nodeList;
    }

    public void setNodeList(List<Node> nodeList) {
        this.nodeList = nodeList;
    }

}
