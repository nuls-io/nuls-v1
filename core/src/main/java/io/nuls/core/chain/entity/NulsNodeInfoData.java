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
    public int size() {
        //todo
        return 1;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        //todo
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) {
        //todo
        return;
    }
}
