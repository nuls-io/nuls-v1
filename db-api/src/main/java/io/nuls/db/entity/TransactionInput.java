package io.nuls.db.entity;

public class TransactionInput {
    private Long id;

    private Long height;

    private String txhash;

    private byte[] script;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getHeight() {
        return height;
    }

    public void setHeight(Long height) {
        this.height = height;
    }

    public String getTxhash() {
        return txhash;
    }

    public void setTxhash(String txhash) {
        this.txhash = txhash;
    }

    public byte[] getScript() {
        return script;
    }

    public void setScript(byte[] script) {
        this.script = script;
    }
}