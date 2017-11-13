package io.nuls.ledger.thread;

import io.nuls.core.context.NulsContext;
import io.nuls.core.thread.NulsThread;
import io.nuls.ledger.module.impl.LedgerModuleImpl;

/**
 * Created by Niels on 2017/11/13.
 * nuls.io
 */
public class SmallChangeThread extends NulsThread {
    public SmallChangeThread(String name) {
        super(NulsContext.getInstance().getModule(LedgerModuleImpl.class), name);
    }

    @Override
    public void run() {

    }
}
