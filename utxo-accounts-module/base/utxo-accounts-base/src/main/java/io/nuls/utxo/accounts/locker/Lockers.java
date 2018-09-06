package io.nuls.utxo.accounts.locker;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Lockers {
    public final static Lock SYN_UTXO_ACCOUNTS_LOCK = new ReentrantLock();
}
