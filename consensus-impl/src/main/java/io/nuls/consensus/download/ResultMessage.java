package io.nuls.consensus.download;

import io.nuls.core.chain.entity.Block;
import io.nuls.network.entity.Node;

import java.util.List;

/**
 * Created by ln on 2018/4/8.
 */
public class ResultMessage {
    private String startHash;
    private String endHash;
    private long startHeight;
    private int size;
    private Node node;

    private List<Block> blockList;

    public ResultMessage(String startHash, String endHash, long startHeight, int size, Node node, List<Block> blockList) {
        this.startHash = startHash;
        this.endHash = endHash;
        this.startHeight = startHeight;
        this.size = size;
        this.node = node;
        this.blockList = blockList;
    }

    public String getStartHash() {
        return startHash;
    }

    public String getEndHash() {
        return endHash;
    }

    public long getStartHeight() {
        return startHeight;
    }

    public int getSize() {
        return size;
    }

    public Node getNode() {
        return node;
    }

    public List<Block> getBlockList() {
        return blockList;
    }
}
