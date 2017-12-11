package io.nuls.consensus.utils;

import io.nuls.core.chain.entity.NulsDigestData;

import java.util.List;

/**
 * @author Niels
 * @date 2017/12/11
 */
public class BestBlockInfo {

    private int height;
    private NulsDigestData hash;
    private List<String> peerIdList;

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public NulsDigestData getHash() {
        return hash;
    }

    public void setHash(NulsDigestData hash) {
        this.hash = hash;
    }

    public List<String> getPeerIdList() {
        return peerIdList;
    }

    public void setPeerIdList(List<String> peerIdList) {
        this.peerIdList = peerIdList;
    }
}
