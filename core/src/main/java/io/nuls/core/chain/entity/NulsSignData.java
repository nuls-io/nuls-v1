package io.nuls.core.chain.entity;

import io.nuls.core.utils.io.ByteBuffer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by facjas on 2017/11/20.
 */
public class NulsSignData extends NulsData{
    protected int signAlgType;

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
    public void parse(ByteBuffer byteBuffer) {

    }
}
