package io.nuls.db.entity;

/**
 * @author Niels
 * @date 2017/11/20
 */
public class TransactionLocalPo {

    private String hash;

    private Integer type;

    private Long createTime;

    private Long blockHeight;

    private Long fee;

    private String remark;

    private int index;

    private byte[] txData;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(Long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public Long getFee() {
        return fee;
    }

    public void setFee(Long fee) {
        this.fee = fee;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public byte[] getTxData() {
        return txData;
    }

    public void setTxData(byte[] txData) {
        this.txData = txData;
    }
}