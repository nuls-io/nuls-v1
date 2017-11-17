package io.nuls.ledger.event;

import io.nuls.core.event.NulsEventHeader;
import io.nuls.core.utils.io.ByteBuffer;
import io.nuls.ledger.entity.utxoTransaction.UtxoDepositTransaction;

/**
 * Created by Niels on 2017/11/17.
 */
public class UtxoDepositEvent<UtxoDepositTransaction> extends BaseUtxoLedgerEvent {

    public UtxoDepositEvent(NulsEventHeader header) {
        super(header);
    }
}
