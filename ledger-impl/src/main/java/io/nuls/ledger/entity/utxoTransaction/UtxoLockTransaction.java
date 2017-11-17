package io.nuls.ledger.entity.utxoTransaction;

import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.utils.io.ByteBuffer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Niels on 2017/11/14.
 */
public class UtxoLockTransaction extends UtxoCoinTransaction {

    private long lockTime;

    public UtxoLockTransaction(){
        this.type = TransactionConstant.TX_TYPE_LOCK;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void serializeToStream(OutputStream stream) throws IOException {
        super.serializeToStream(stream);
        stream.write(new VarInt(lockTime).encode());
    }

    @Override
    public void parse(ByteBuffer byteBuffer) {

    }
}
