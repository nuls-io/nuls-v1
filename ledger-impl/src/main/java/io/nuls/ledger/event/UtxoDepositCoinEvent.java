package io.nuls.ledger.event;

import io.nuls.core.event.NulsEventHeader;
import io.nuls.ledger.entity.utxoTransaction.UtxoDepositTransaction;
import javafx.event.EventType;

/**
 * Created by Niels on 2017/11/17.
 */
public class UtxoDepositCoinEvent<T extends UtxoDepositTransaction> extends LockCoinEvent<T> {

    public UtxoDepositCoinEvent() {
        super();
    }
}
