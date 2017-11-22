package io.nuls.core.chain.entity;

import io.nuls.core.utils.io.NulsByteBuffer;

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
    public int size() {
        return 0;
    }

    @Override
    public void serializeToStream(OutputStream stream) throws IOException {

    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {

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
