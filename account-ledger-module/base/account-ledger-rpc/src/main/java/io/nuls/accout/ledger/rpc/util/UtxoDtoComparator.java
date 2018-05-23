package io.nuls.accout.ledger.rpc.util;

import io.nuls.accout.ledger.rpc.dto.UtxoDto;

import java.util.Comparator;

public class UtxoDtoComparator implements Comparator<UtxoDto> {

    private static UtxoDtoComparator instance = new UtxoDtoComparator();

    private UtxoDtoComparator() {

    }

    public static UtxoDtoComparator getInstance() {
        return instance;
    }

    @Override
    public int compare(UtxoDto o1, UtxoDto o2) {
        if (o1.getCreateTime() < o2.getCreateTime()) {
            return 1;
        } else if (o1.getCreateTime() > o2.getCreateTime()) {
            return -1;
        }
        return 0;
    }
}
