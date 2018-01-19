package io.nuls.core.chain.entity;

import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;
import java.util.List;

/**
 * author Facjas
 * date 2018/1/2.
 */
public class NulsSignDataGroup extends BaseNulsData{

    private int count = 0;
    private List<NulsSignData> signList;

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {

    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {

    }
}
