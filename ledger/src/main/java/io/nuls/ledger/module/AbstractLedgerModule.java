package io.nuls.ledger.module;

import io.nuls.core.constant.NulsConstant;
import io.nuls.core.module.BaseModuleBootstrap;
import io.nuls.ledger.event.AbstractCoinTransactionEvent;

/**
 *
 * @author Niels
 * @date 2017/11/7
 */
public abstract class AbstractLedgerModule extends BaseModuleBootstrap {
    public AbstractLedgerModule() {
        super(NulsConstant.MODULE_ID_LEDGER);
    }
}
