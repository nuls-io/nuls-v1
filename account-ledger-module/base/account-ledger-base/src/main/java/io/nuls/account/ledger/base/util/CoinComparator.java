package io.nuls.account.ledger.util;

import io.nuls.kernel.model.Coin;

import java.util.Comparator;

public class CoinComparator implements Comparator<Coin> {

    private static CoinComparator instance = new CoinComparator();

    private CoinComparator() {

    }

    public static CoinComparator getInstance() {
        return instance;
    }

    @Override
    public int compare(Coin o1, Coin o2) {
        return o1.getNa().compareTo(o2.getNa());
    }
}
