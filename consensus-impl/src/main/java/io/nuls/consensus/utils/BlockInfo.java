package io.nuls.consensus.utils;

import io.nuls.core.chain.entity.NulsDigestData;

import java.util.*;

/**
 * @author Niels
 * @date 2017/12/11
 */
public class BlockInfo {
    private long bestHeight ;
    private NulsDigestData bestHash ;
    private Map<Long, NulsDigestData> heightHashMap = new HashMap<>();
    private List<String> nodeIdList;
    private boolean finished = false;


    public NulsDigestData getHash(long height) {
        return heightHashMap.get(height);
    }

    public void putHash(long height, NulsDigestData hash) {
        heightHashMap.put(height, hash);
    }

    public long getBestHeight() {
        return bestHeight;
    }

    public void setBestHeight(long bestHeight) {
        this.bestHeight = bestHeight;
    }

    public NulsDigestData getBestHash() {
        return bestHash;
    }

    public void setBestHash(NulsDigestData bestHash) {
        this.bestHash = bestHash;
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
