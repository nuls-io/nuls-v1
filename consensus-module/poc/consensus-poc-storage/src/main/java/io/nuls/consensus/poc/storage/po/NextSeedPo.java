package io.nuls.consensus.poc.storage.po;

import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.Address;
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.NulsOutputStreamBuffer;

import java.io.IOException;

/**
 * @author Niels
 */
public class NextSeedPo extends BaseNulsData {

    private byte[] address;

    private long height;

    private byte[] seedHash;

    private byte[] nextSeed;

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public byte[] getNextSeed() {
        return nextSeed;
    }

    public void setNextSeed(byte[] nextSeed) {
        this.nextSeed = nextSeed;
    }

    public byte[] getSeedHash() {
        return seedHash;
    }

    public void setSeedHash(byte[] seedHash) {
        this.seedHash = seedHash;
    }

    public byte[] getAddress() {
        return address;
    }

    public void setAddress(byte[] address) {
        this.address = address;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeInt64(height);
        stream.write(nextSeed);
        stream.write(seedHash);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        height = byteBuffer.readInt64();
        nextSeed = byteBuffer.readBytes(32);
        seedHash = byteBuffer.readBytes(8);
    }

    @Override
    public int size() {
        return 0;
    }
}
