package io.nuls.contract.storage.po;

import java.util.Map;
import java.util.Set;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/8/15
 */
public class ContractCollectionInfoPo {

    private String contractAddress;
    private byte[] creater;
    private Map<String, String> collectorMap;
    private long createTime;
    private long blockHeight;

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public byte[] getCreater() {
        return creater;
    }

    public void setCreater(byte[] creater) {
        this.creater = creater;
    }

    public Map<String, String> getCollectorMap() {
        return collectorMap;
    }

    public void setCollectorMap(Map<String, String> collectorMap) {
        this.collectorMap = collectorMap;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public int compareTo(long thatTime) {
        if(this.createTime > thatTime) {
            return -1;
        } else if(this.createTime < thatTime) {
            return 1;
        }
        return 0;
    }
}
