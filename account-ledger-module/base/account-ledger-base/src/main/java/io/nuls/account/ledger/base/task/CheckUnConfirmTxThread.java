package io.nuls.account.ledger.base.task;


import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;

@Component
public class CheckUnConfirmTxThread implements Runnable {

    @Autowired
    private AccountLedgerService accountLedgerService;
    @Override
    public void run() {
          //  accountLedgerService.getAllUnconfirmedTransaction()
    }
}
