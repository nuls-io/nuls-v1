package io.nuls.ledger.intf;

import java.util.List;

public interface INulsLedger {
    long getBalance(String accountId);
    long getBalance(List<String> accountIds);
    boolean transfer(String fromAccountId,String toAccountId,long amount);


}
