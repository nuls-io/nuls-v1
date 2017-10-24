package io.nuls.account.service.intf;


import io.nuls.account.entity.Account;

public interface AccountManager {
    Account createAccount();
    Account createAccountFromPriKey(String Key);
    Account createAccountFromPriKey(String Key, String algType);
    byte [] seriallize();
    Account parse(byte[] bytes, int index);
}
