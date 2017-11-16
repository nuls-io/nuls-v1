package io.nuls.ledger.event;

import io.nuls.core.chain.entity.NulsData;
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
    protected NulsData parseEventBody(ByteBuffer byteBuffer) {
        //todo
        return null;
    }


}
