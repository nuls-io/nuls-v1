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
public class NulsNodeInfoData extends BaseNulsData {

    @Override
    protected int dataSize() {
        //todo
        return 1;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        //todo
    }

    @Override
    protected void parseObject(NulsByteBuffer byteBuffer) {
        //todo
        return;
    }
}
