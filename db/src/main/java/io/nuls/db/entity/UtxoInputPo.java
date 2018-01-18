package io.nuls.db.entity;

/**
 * @author Niels
 * @date 2017/11/20
 */
public class UtxoInputPo {

    private String txHash;

    private Integer inIndex;

    private Integer fromIndex;

    private byte[] sign;

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public Integer getInIndex() {
        return inIndex;
    }

    public void setInIndex(Integer inIndex) {
        this.inIndex = inIndex;
    }

    public Integer getFromIndex() {
        return fromIndex;
    }

    public void setFromIndex(Integer fromIndex) {
        this.fromIndex = fromIndex;
    }

    public byte[] getSign() {
        return sign;
    }

    public void setSign(byte[] sign) {
        this.sign = sign;
    }
}