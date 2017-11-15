package io.nuls.account.service.intf;


import io.nuls.account.entity.Account;
import io.nuls.account.entity.Address;

import java.util.List;

public interface AccountService {
    Account createAccount();

    void resetKey(Account account,String password);

    Account getLocalAccount();

    List<Account> getLocalAccountList();

    Account getAccount(String address);

    Address getAddress(String pubKey);

    byte[] getPriKey(String address);

    void switchAccount(String id);
}
