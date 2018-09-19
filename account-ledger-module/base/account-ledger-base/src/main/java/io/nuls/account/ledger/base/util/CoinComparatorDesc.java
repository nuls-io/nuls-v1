package io.nuls.account.ledger.base.util;

import io.nuls.kernel.model.Coin;

import java.util.Comparator;

public class CoinComparatorDesc implements Comparator<Coin> {
    private static CoinComparatorDesc instance = new CoinComparatorDesc();

    private CoinComparatorDesc() {

    }

    public static CoinComparatorDesc getInstance() {
        return instance;
    }

    @Override
    public int compare(Coin o1, Coin o2) {
        if(o1 == null) {
            return -1;
        }
        if(o2 == null) {
            return 1;
        }
        return o2.getNa().compareTo(o1.getNa());
    }
}
