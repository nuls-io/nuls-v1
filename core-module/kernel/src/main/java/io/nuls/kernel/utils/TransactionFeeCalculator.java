package io.nuls.kernel.utils;

import io.nuls.kernel.model.Na;

/**
 * @author Niels
 * @date 2018/5/15
 */
public class TransactionFeeCalculator {

    public static final Na PRECE_PRE_1000_bytes = Na.valueOf(1000000);

    /**
     * 根据交易大小计算需要交纳的手续费
     * According to the transaction size calculate the handling fee.
     *
     * @param size 交易大小/size of the transaction
     * @return
     */
    public static final Na getFee(int size) {
        Na fee = PRECE_PRE_1000_bytes.multiply(size / 1000);
        if (size % 1000 > 0) {
            fee = fee.add(PRECE_PRE_1000_bytes);
        }
        return fee;
    }
}
