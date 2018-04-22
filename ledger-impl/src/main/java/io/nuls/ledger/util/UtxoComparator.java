package io.nuls.ledger.util;

import io.nuls.ledger.entity.UtxoOutput;

import java.util.Comparator;

public class UtxoComparator implements Comparator<UtxoOutput> {

    private static final UtxoComparator instance = new UtxoComparator();

    private UtxoComparator() {

    }

    public static UtxoComparator getInstance() {
        return instance;
    }

    @Override
    public int compare(UtxoOutput o1, UtxoOutput o2) {
        if (o1.getValue() < o2.getValue()) {
            return -1;
        } else if (o1.getValue() == o2.getValue()) {
            return 0;
        }
        return 1;
    }
}
