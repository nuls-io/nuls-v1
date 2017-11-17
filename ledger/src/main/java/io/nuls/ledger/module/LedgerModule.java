package io.nuls.ledger.module;

import io.nuls.core.module.NulsModule;

/**
 * Created by Niels on 2017/11/7.
 *
 */
public abstract class LedgerModule extends NulsModule {
    public LedgerModule() {
        super("ledger");
    }
}
