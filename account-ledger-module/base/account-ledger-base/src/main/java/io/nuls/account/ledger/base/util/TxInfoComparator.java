package io.nuls.account.ledger.base.util;

import io.nuls.account.ledger.model.TransactionInfo;

import java.util.Comparator;

public class TxInfoComparator implements Comparator<TransactionInfo> {

    private TxInfoComparator() {

    }

    private static TxInfoComparator instance = new TxInfoComparator();

    public static TxInfoComparator getInstance() {
        return instance;
    }

    @Override
    public int compare(TransactionInfo o1, TransactionInfo o2) {
        if (o1.getTime() < o2.getTime()) {
            return -1;
        } else if (o1.getTime() > o2.getTime()) {
            return 1;
        }
        return 0;
    }
}
