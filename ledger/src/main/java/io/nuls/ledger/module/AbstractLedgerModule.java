package io.nuls.ledger.module;

import io.nuls.core.constant.NulsConstant;
import io.nuls.core.module.BaseNulsModule;
import io.nuls.ledger.event.BaseLedgerEvent;
import io.nuls.ledger.event.AbstractCoinTransactionEvent;

/**
 *
 * @author Niels
 * @date 2017/11/7
 */
public abstract class AbstractLedgerModule extends BaseNulsModule {
    public AbstractLedgerModule() {
        super(NulsConstant.MODULE_ID_LEDGER);
        this.registerEvent((short) 1, BaseLedgerEvent.class);
        this.registerEvent((short) 2, AbstractCoinTransactionEvent.class);
    }
}
