package io.nuls.db.entity;

/**
 * @author Niels
 * @date 2017/11/20
 */
public class TransactionPo {

    private String hash;

    private Integer type;

    private Long createTime;

    private Integer blockHeight;

    private String blockHash;

    private byte[] txdata;


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

    public Integer getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(Integer blockHeight) {
        this.blockHeight = blockHeight;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public byte[] getTxdata() {
        return txdata;
    }

    public void setTxdata(byte[] txdata) {
        this.txdata = txdata;
    }
}