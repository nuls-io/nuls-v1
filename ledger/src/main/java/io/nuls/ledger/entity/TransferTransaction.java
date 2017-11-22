package io.nuls.ledger.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.utils.io.NulsByteBuffer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Niels on 2017/11/20.
 */
public class TransferTransaction<T extends BaseNulsData> extends AbstractCoinTransaction<T> {
    public TransferTransaction(){
        this.type = TransactionConstant.TX_TYPE_TRANSFER;
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
