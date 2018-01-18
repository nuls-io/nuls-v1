package io.nuls.db.entity;

/**
 * @author Niels
 * @date 2017/11/20
 */
public class UtxoOutputPo {

    private String txHash;

    private Integer outIndex;

    private Long value;

    private Long lockTime;

    private Byte status;

    private String address;

    private byte[] script;

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public Integer getOutIndex() {
        return outIndex;
    }

    public void setOutIndex(Integer outIndex) {
        this.outIndex = outIndex;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public Long getLockTime() {
        return lockTime;
    }

    public void setLockTime(Long lockTime) {
        this.lockTime = lockTime;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public byte[] getScript() {
        return script;
    }

    public void setScript(byte[] script) {
        this.script = script;
    }
}