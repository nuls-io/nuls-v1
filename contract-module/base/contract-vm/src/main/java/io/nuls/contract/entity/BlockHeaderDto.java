package io.nuls.contract.entity;


import io.nuls.core.tools.crypto.Hex;
import io.nuls.kernel.model.BlockHeader;

import java.io.IOException;
import java.io.Serializable;

/**
 * @Desription:
 * @Author: PierreLuo
 * @Date: 2018/5/2
 */
public class BlockHeaderDto implements Serializable {

    private String hash;
    private long time;
    private long height;
    //23 bytes
    private byte[] packingAddress;
    private byte[] stateRoot;

    public BlockHeaderDto() {}

    public BlockHeaderDto(BlockHeader header) throws IOException {
        this.hash = header.getHash().getDigestHex();
        this.time = header.getTime();
        this.height = header.getHeight();
        this.packingAddress = header.getPackingAddress();
        this.stateRoot = header.getStateRoot();
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public byte[] getPackingAddress() {
        return packingAddress;
    }

    public void setPackingAddress(byte[] packingAddress) {
        this.packingAddress = packingAddress;
    }

    public byte[] getStateRoot() {
        return stateRoot;
    }

    public void setStateRoot(byte[] stateRoot) {
        this.stateRoot = stateRoot;
    }
}
