package io.nuls.core.chain.entity;

/**
 * @author win10
 */
public class Chain {
    private String chainName = "";
    private long magicNumber = 12345678L;

    private int addressPrefix = 88;
    public String getChainName() {
        return chainName;
    }

    public void setChainName(String chainName) {
        this.chainName = chainName;
    }
}
