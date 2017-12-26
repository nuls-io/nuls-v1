package io.nuls.consensus.utils;

import io.nuls.core.chain.entity.Transaction;

import java.util.Comparator;

/**
 * @author Niels
 * @date 2017/12/26
 */
public class TxComparator implements Comparator<Transaction> {


    @Override
    public int compare(Transaction o1, Transaction o2) {
        long key = o1.size() - o2.size();
        int val = 0;
        if (key > 0) {
            return 1;
        } else if (key < 0) {
            return -1;
        }
        return val;
    }
}
