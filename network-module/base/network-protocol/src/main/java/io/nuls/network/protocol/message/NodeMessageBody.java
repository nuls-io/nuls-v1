package io.nuls.network.protocol.message;

import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.NulsOutputStreamBuffer;
import io.nuls.network.entity.Node;
import io.protostuff.Tag;

import java.io.IOException;
import java.util.List;

public class NodeMessageBody extends BaseNulsData{


    private int length;

    private List<String> ipList;

    private List<Node> nodeList;

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public List<String> getIpList() {
        return ipList;
    }

    public void setIpList(List<String> ipList) {
        this.ipList = ipList;
    }

    public List<Node> getNodeList() {
        return nodeList;
    }

    public void setNodeList(List<Node> nodeList) {
        this.nodeList = nodeList;
    }

    /**
     * serialize important field
     */
    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        // todo auto-generated method stub

    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        // todo auto-generated method stub

    }

    @Override
    public int size() {
        // todo auto-generated method stub
        return 0;
    }
}
