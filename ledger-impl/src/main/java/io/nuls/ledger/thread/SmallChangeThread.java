package io.nuls.ledger.thread;

import io.nuls.account.service.intf.AccountService;
import io.nuls.core.context.NulsContext;
import io.nuls.core.thread.NulsThread;
import io.nuls.core.utils.log.Log;
import io.nuls.ledger.module.impl.UtxoLedgerModuleImpl;
import io.nuls.ledger.service.intf.LedgerService;

/**
 * Created by Niels on 2017/11/13.
 */
public class SmallChangeThread extends NulsThread {
    private LedgerService ledgerService = NulsContext.getInstance().getService(LedgerService.class);
    private AccountService accountService = NulsContext.getInstance().getService(AccountService.class);
    private static final SmallChangeThread instance = new SmallChangeThread(SmallChangeThread.class.getSimpleName());

    private SmallChangeThread(String name) {
        super(NulsContext.getInstance().getModule(UtxoLedgerModuleImpl.class), name);
    }

    public static SmallChangeThread getInstance() {
        return instance;
    }

    private boolean stop;

    @Override
    public void run() {
        stop = false;
        while (!stop) {
            try {
                smallChange();
            } catch (Exception e) {
                Log.error(e);
            }
            try {
                Thread.sleep(3600000L);
            } catch (InterruptedException e) {
                Log.error(e);
            }
        }
    }

    private void smallChange() {
        //todo Statistical non spending
//        List<TransactionOutput> list = ledgerService.queryNotSpent(this.accountService.getLocalAccount().getAddress().toString());
//        int count = list.size();
//        if (count >= LedgerConstant.SMALL_CHANGE_COUNT) {
//            this.ledgerService.smallChange(list.subList(0, LedgerConstant.SMALL_CHANGE_COUNT));
//        }
    }

    public void stop() {
        this.stop = true;
    }


}
