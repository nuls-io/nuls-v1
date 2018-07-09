package io.nuls.ledger.sdk.model;

import io.nuls.sdk.exception.NulsException;
import io.nuls.sdk.model.BaseNulsData;
import io.nuls.sdk.utils.NulsByteBuffer;
import io.nuls.sdk.utils.NulsOutputStreamBuffer;

import java.io.IOException;

public class Coin extends BaseNulsData {




    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {

    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {

    }

    @Override
    public int size() {
        return 0;
    }
}
