package io.nuls.consensus.utils;

import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.entity.NulsDigestData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/12/11
 */
public class DistributedBestHeightCalcCache {

    private List<String> peerIdList;
    private Map<String,BlockHeader> headerMap = new HashMap<>();

    public List<String> getPeerIdList() {
        return peerIdList;
    }

    public void setPeerIdList(List<String> peerIdList) {
        this.peerIdList = peerIdList;
    }

    public int getHeight() {
        return getBestBlockInfo().getHeight();
    }

    public NulsDigestData getHash(){
        return getBestBlockInfo().getHash();
    }

    public void addBlockHeader(String peerId,BlockHeader header) {
        headerMap.put(peerId,header);
        calc();
    }

    private void calc() {
        //todo
    }

    public BestBlockInfo getBestBlockInfo() {
        //todo
        return null;
    }
}
