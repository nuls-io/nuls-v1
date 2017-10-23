package io.nuls.db.entity;

/**
 * Created by win10 on 2017/10/17.
 */
public class BlockHeader {

    private String hash;

    private Long height;

    private Long createtime;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Long getHeight() {
        return height;
    }

    public void setHeight(Long height) {
        this.height = height;
    }

    public Long getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Long createtime) {
        this.createtime = createtime;
    }
}
