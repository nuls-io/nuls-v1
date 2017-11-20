package io.nuls.core.chain.entity;


import io.nuls.core.utils.io.NulsByteBuffer;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by facjas on 2017/11/20.
 */
public class NulsDigestData extends NulsData{
    protected int digestAlgType;

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
}
