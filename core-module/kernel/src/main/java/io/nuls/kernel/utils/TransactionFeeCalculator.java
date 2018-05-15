package io.nuls.kernel.utils;

import io.nuls.kernel.model.Na;

/**
 * @author Niels
 * @date 2018/5/15
 */
public class TransactionFeeCalculator {

    public static final Na PRECE_PRE_100_bytes = Na.valueOf(100000);

    public static final Na getFee(int size) {
        Na fee = PRECE_PRE_100_bytes.multiply(size / 100);
        if (size % 100 > 0) {
            fee = fee.add(PRECE_PRE_100_bytes);
        }
        return fee;
    }
}
