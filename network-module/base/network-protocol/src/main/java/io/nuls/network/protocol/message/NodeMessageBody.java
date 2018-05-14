package io.nuls.network.protocol.message;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.NulsOutputStreamBuffer;
import io.nuls.kernel.utils.SerializeUtils;
import io.nuls.kernel.utils.VarInt;
import io.nuls.network.entity.Node;
import io.protostuff.Tag;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class NodeMessageBody extends BaseNulsData {

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
        stream.writeVarInt(length);
        int count = ipList == null ? 0 : ipList.size();
        stream.writeVarInt(count);
        if (null != ipList) {
            for (String ip : ipList) {
                stream.writeString(ip);
            }
        }
        count = nodeList == null ? 0 : nodeList.size();
        stream.writeVarInt(count);
        if (null != nodeList) {
            for (Node node : nodeList) {
                stream.writeNulsData(node);
            }
        }
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        length = (int) byteBuffer.readVarInt();

        List<String> ipList = new ArrayList<>();
        int count = (int) byteBuffer.readVarInt();
        for (int i = 0; i < count; i++) {
            ipList.add(byteBuffer.readString());
        }
        this.ipList = ipList;

        List<Node> nodeList = new ArrayList<>();
        count = (int) byteBuffer.readVarInt();
        for (int i = 0; i < count; i++) {
            nodeList.add(byteBuffer.readNulsData(new Node()));
        }
        this.nodeList = nodeList;
    }

    @Override
    public int size() {
        int s = 0;
        s += VarInt.sizeOf(length);
        s += SerializeUtils.sizeOfVarInt(ipList == null ? 0 : ipList.size());
        if (null != ipList) {
            for (String ip : ipList) {
                s += SerializeUtils.sizeOfString(ip);
            }
        }

        s += SerializeUtils.sizeOfVarInt(nodeList == null ? 0 : nodeList.size());
        if (nodeList != null) {
            for (Node node : nodeList) {
                s += node.size();
            }
        }
        return s;
    }
}
