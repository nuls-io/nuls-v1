package io.nuls.account.intf;

import io.nuls.account.Account;

public interface IAccountManager {
    Account createAccount();
    Account createAccountFromPriKey(String Key);
    Account createAccountFromPriKey(String Key, String algType);
    byte [] seriallize();
    Account parse(byte[] bytes, int index);
}
