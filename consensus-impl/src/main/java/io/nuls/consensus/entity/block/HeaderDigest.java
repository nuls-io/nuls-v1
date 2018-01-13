package io.nuls.consensus.entity.block;

/**
 * @author Niels
 * @date 2018/1/13
 */
public class HeaderDigest {
    private String hash;
    private long height;

    public HeaderDigest(String hash, long height) {
        this.hash = hash;
        this.height = height;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HeaderDigest)) {
            return false;
        }
        return this.getHeight() == ((HeaderDigest) obj).getHeight() && this.getHash().equals(((HeaderDigest) obj).getHash());
    }
}
