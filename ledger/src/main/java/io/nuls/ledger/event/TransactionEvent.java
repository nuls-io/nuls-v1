package io.nuls.ledger.event;

import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.event.NulsEventHeader;
import io.nuls.core.utils.io.ByteBuffer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Niels on 2017/11/8.
 *
 */
public class TransactionEvent extends BaseLedgerEvent {
    private Transaction tx;

    public Transaction getTransaction(){
        return tx;
    }

    public TransactionEvent(NulsEventHeader header) {
        super(header);
    }

    @Override
    public int size() {
        //todo
        return 0;
    }

    @Override
    public void serializeToStream(OutputStream stream) throws IOException {
        //todo
    }

    @Override
    public void parse(ByteBuffer byteBuffer) {
        this.tx = null;
        //todo
    }

}
