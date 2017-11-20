package io.nuls.core.chain.entity;

import io.nuls.core.utils.io.NulsByteBuffer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by facjas on 2017/11/20.
 */
public class NulsNodeInfoData extends NulsData {

    @Override
    public int size() {
        return 1;
    }

    @Override
    public void serializeToStream(OutputStream stream) throws IOException {
        stream.write("a".getBytes());
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        return;
    }
}
