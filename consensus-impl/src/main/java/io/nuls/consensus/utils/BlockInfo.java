package io.nuls.consensus.utils;

import io.nuls.core.chain.entity.NulsDigestData;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Niels
 * @date 2017/12/11
 */
public class BlockInfo {

    private long height;
    private NulsDigestData hash;
    private List<String> nodeIdList;
    private boolean finished = false;
    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public NulsDigestData getHash() {
        return hash;
    }

    public void setHash(NulsDigestData hash) {
        this.hash = hash;
    }

    public List<String> getNodeIdList() {
        return nodeIdList;
    }

    public void setNodeIdList(List<String> nodeIdList) {
        this.nodeIdList = nodeIdList;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}
