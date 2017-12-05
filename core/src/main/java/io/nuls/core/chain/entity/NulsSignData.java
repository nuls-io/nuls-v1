package io.nuls.core.chain.entity;

import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author facjas
 * @date 2017/11/20
 */
public class NulsSignData extends BaseNulsData{
    protected int signAlgType;
    protected int signLength;
    protected byte[] signBytes;

    public int getSignAlgType() {
        return signAlgType;
    }

    public void setSignAlgType(int signAlgType) {
        this.signAlgType = signAlgType;
    }


    @Override
    protected int dataSize() {
        return 0;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {

    }

    @Override
    protected void parseObject(NulsByteBuffer byteBuffer) {

    }

    public int getSignLength() {
        return signLength;
    }

    public void setSignLength(int signLength) {
        this.signLength = signLength;
    }

    public byte[] getSignBytes() {
        return signBytes;
    }

    public void setSignBytes(byte[] signBytes) {
        this.signBytes = signBytes;
    }
}
