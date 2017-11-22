package io.nuls.core.chain.entity;


import io.nuls.core.utils.io.NulsByteBuffer;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author facjas
 * @date 2017/11/20
 */
public class NulsDigestData extends BaseNulsData{

    protected int digestAlgType;
    protected int digestLength;
    protected byte[] digestBytes;

    public int getDigestAlgType() {
        return digestAlgType;
    }

    public void setDigestAlgType(int digestAlgType) {
        this.digestAlgType = digestAlgType;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void serializeToStream(OutputStream stream) throws IOException {

    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {

    }

    public int getDigestLength() {
        return digestLength;
    }

    public void setDigestLength(int digestLength) {
        this.digestLength = digestLength;
    }

    public byte[] getDigestBytes() {
        return digestBytes;
    }

    public void setDigestBytes(byte[] digestBytes) {
        this.digestBytes = digestBytes;
    }
}
