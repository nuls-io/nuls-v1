package io.nuls.consensus.poc.storage.po;

import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.NulsOutputStreamBuffer;

import java.io.IOException;

/**
 * @author Niels
 */
public class RandomSeedPo extends BaseNulsData {
    private long height;

    private byte[] seed;

    private byte[] nextSeedHash;

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public byte[] getSeed() {
        return seed;
    }

    public void setSeed(byte[] seed) {
        this.seed = seed;
    }

    public byte[] getNextSeedHash() {
        return nextSeedHash;
    }

    public void setNextSeedHash(byte[] nextSeedHash) {
        this.nextSeedHash = nextSeedHash;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(seed);
        stream.write(nextSeedHash);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.seed = byteBuffer.readBytes(32);
        this.nextSeedHash = byteBuffer.readBytes(8);
    }

    @Override
    public int size() {
        return 40;
    }
}
