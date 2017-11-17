package io.nuls.ledger.event;

import io.nuls.core.event.NulsEventHeader;
import io.nuls.core.utils.io.ByteBuffer;
import io.nuls.ledger.entity.DepositTransaction;

/**
 * Created by Niels on 2017/11/17.
 */
public class DepositEvent extends CoinTransactionEvent<DepositTransaction> {
    public DepositEvent(NulsEventHeader header) {
        super(header);
    }

    @Override
    protected DepositTransaction parseEventBody(ByteBuffer byteBuffer) {
        return null;
    }
}
