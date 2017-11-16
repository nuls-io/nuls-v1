package io.nuls.ledger.entity;

import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.utils.io.ByteBuffer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Niels on 2017/11/14.
 */
public class SmallChangeTransaction extends CoinTransaction<UtxoData> {

    public SmallChangeTransaction() {
        this.type = TransactionConstant.TX_TYPE_SMALL_CHANGE;
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
