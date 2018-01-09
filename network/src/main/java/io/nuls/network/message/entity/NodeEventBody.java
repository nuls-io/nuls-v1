package io.nuls.network.message.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.NulsVersion;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.network.entity.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author vivi
 * @date 2017/12/5.
 */
public class NodeEventBody extends BaseNulsData {

    public static final short OWN_MAIN_VERSION = 1;

    public static final short OWN_SUB_VERSION = 1001;

    private List<Node> nodes;

    public NodeEventBody() {
        //  super(OWN_MAIN_VERSION, OWN_SUB_VERSION);
        this.nodes = new ArrayList<>();
    }

    public NodeEventBody(List nodes) {
      //  super(OWN_MAIN_VERSION, OWN_SUB_VERSION);
        this.nodes = nodes;
    }

    @Override
    public int size() {
        int s = 2;   //version size;
        s += VarInt.sizeOf(nodes.size());
        for (Node node : nodes) {
            s += node.size();
        }
        return s;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeShort(version.getVersion());
        stream.write(new VarInt(nodes.size()).encode());
        for (Node node : nodes) {
            stream.writeNulsData(node);
        }
    }

    @Override
    protected void parse(NulsByteBuffer buffer) throws NulsException {
        version = new NulsVersion(buffer.readShort());
        int size = (int) buffer.readVarInt();
        for (int i = 0; i < size; i++) {
            nodes.add(buffer.readNulsData(new Node()));
        }
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }
}
